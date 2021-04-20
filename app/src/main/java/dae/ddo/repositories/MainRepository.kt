package dae.ddo.repositories

import android.content.res.AssetManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dae.ddo.BuildConfig
import dae.ddo.db.BaseDatabase
import dae.ddo.entities.*
import dae.ddo.net.DdoService
import dae.ddo.state.NetworkState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.aaronhe.threetengson.ThreeTenGsonAdapter
import org.threeten.bp.Instant
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

class MainRepository(
    private val db: BaseDatabase,
    private val assetManager: AssetManager
) {
    private val service: DdoService

    val networkState: MutableStateFlow<NetworkState> = MutableStateFlow(NetworkState.None)

    init {
        val builder = GsonBuilder()
        val gson: Gson = ThreeTenGsonAdapter.registerLocalDateTime(builder).create()

        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor())
            .addNetworkInterceptor {
                it.proceed(
                    it.request().newBuilder()
                        .header(
                            "User-Agent",
                            "assemble/${BuildConfig.VERSION_NAME}/${BuildConfig.VERSION_CODE}/${BuildConfig.BUILD_TYPE}"
                        )
                        .build()
                )
            }
            .build()

        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl("https://www.playeraudit.com/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        service = retrofit.create(DdoService::class.java)
    }

    fun quests(): Flow<List<Quest>> {
        return db.questDao().flowCount().transform { value ->
            if (value > 0) {
                db.questDao().flowAll()
            } else {
                emit(questsFromAssets())
            }
        }
    }

    fun parties() = db.partyDao().flowAll()

    fun filtersAndConditions() = db.filterDao().flowAllFilterConditions()

    fun conditions(filterId: Long) = db.filterDao().flowAllConditions(filterId)

    suspend fun totalQuests() = withContext(Dispatchers.IO) { db.questDao().count() }

    suspend fun refreshQuests(): List<Quest> {
        try {
            networkState.value = NetworkState.Loading

            Timber.i("Refreshing quests")
            val resp = withContext(Dispatchers.IO) {
                try {
                    service.compendium()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Response.error(500, "Invalid quest JSON: $e".toResponseBody())
                }
            }

            if (!resp.isSuccessful) {
                Timber.e("No quests: ${resp.code()}, ${resp.message()}")
                Timber.w("Use quest list from assets temporarily")
                return emptyList()
            }

            val quests = resp.body() ?: return emptyList()

            Timber.v("Quests: ${quests.map { it.name }}")

            val questList = quests.first().quests.map {
                Quest(
                    0,
                    it.name,
                    it.patron,
                    it.heroicCR,
                    it.epicCR,
                    it.raid
                )
            }

            withContext(Dispatchers.IO) {
                db.questDao().insertAll(questList)
            }

            return withContext(Dispatchers.IO) {
                db.questDao().getAll()
            }
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            networkState.value = NetworkState.None
        }

        return emptyList()
    }

    private suspend fun questsFromAssets(): List<Quest> {
        try {
            Timber.i("Get raw quest list from assets…")
            return withContext(Dispatchers.IO) {
                try {
                    val quests = mutableListOf<Quest>()

                    @Suppress("BlockingMethodInNonBlockingContext")
                    assetManager
                        .open("Quest.csv")
                        .bufferedReader()
                        .lineSequence()
                        .forEachIndexed { index, data ->
                            if (index == 0) return@forEachIndexed
                            val values = data.split("\t")

                            quests.add(
                                Quest(
                                    id = values[0].toInt(),
                                    name = values[1],
                                    patron = if (values[2].isBlank()) null else values[2],
                                    heroicCR = values[3].toInt(),
                                    epicCR = values[4].toIntOrNull(),
                                    raid = values[5].toBoolean()
                                )
                            )
                        }


                    quests
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        return emptyList()
    }

    suspend fun saveFilter(filter: FilterConditions): Long? {
        return withContext(Dispatchers.IO) {
            Timber.v("Save filter $filter")
            db.filterDao().insert(filter)
        }
    }

    suspend fun saveCondition(condition: Condition): List<Long> {
        return withContext(Dispatchers.IO) {
            Timber.v("Save condition $condition")
            db.filterDao().insertAllConditions(condition)
        }
    }

    fun getFilterConditions(filterId: Long) = db.filterDao().getFilterConditions(filterId)

    fun getCondition(conditionId: Long) = db.filterDao().getCondition(conditionId)

    suspend fun deleteFilter(filterId: Long) {
        Timber.v("Delete filter $filterId")
        withContext(Dispatchers.IO) {
            db.filterDao().deleteFilterById(filterId)
        }
    }

    suspend fun deleteCondition(conditionId: Long) {
        Timber.v("Delete condition $conditionId")
        withContext(Dispatchers.IO) {
            db.filterDao().deleteConditionById(conditionId)
        }
    }

    suspend fun refreshParties(): List<Party> {
        try {
            networkState.value = NetworkState.Loading

            Timber.i("Refreshing parties")
            val resp = withContext(Dispatchers.IO) {
                try {
                    service.serverGroups()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Response.error(500, "Invalid party JSON".toResponseBody())
                }
            }

            if (!resp.isSuccessful) {
                Timber.e("No parties: ${resp.code()}, ${resp.message()}")
                return emptyList()
            }

            val servers = resp.body() ?: return emptyList()
//            Timber.v("Parties: ${servers.map { it.parties.map { it.comment } }}")

            val parties = servers.flatMap { server ->
                server.parties.map { party ->
                    Party(
                        party.id,
                        server.name,
                        party.comment,
                        if (party.quest != null) PartyQuest(
                            party.quest.hexId,
                            party.quest.name,
                            party.quest.heroicNormalCR,
                            party.quest.epicNormalCR,
                            party.quest.heroicNormalXp,
                            party.quest.heroicHardXp,
                            party.quest.heroicEliteXp,
                            party.quest.epicNormalXp,
                            party.quest.epicHardXp,
                            party.quest.epicEliteXp,
                            party.quest.isFreeToVip,
                            party.quest.requiredAdventurePack,
                            party.quest.adventureArea,
                            party.quest.questJournalGroup,
                            party.quest.groupSize,
                            party.quest.patron
                        ) else null,
                        party.difficulty,
//                        party.acceptedClasses,
                        party.minimumLevel,
                        party.maximumLevel,
                        party.adventureActive,
                        Player(
                            party.leader.name,
                            party.leader.gender,
                            party.leader.race,
                            party.leader.totalLevel,
                            party.leader.classes.map { clazz ->
                                Class(
                                    clazz.name,
                                    clazz.level
                                )
                            },
                            Location(
                                party.leader.location.name,
                                party.leader.location.region,
                                party.leader.location.hexId,
                                party.leader.location.publicSpace
                            ),
                            party.leader.guild,
                            party.leader.homeServer
                        ),
                        party.members.map { member ->
                            Player(
                                member.name,
                                member.gender,
                                member.race,
                                member.totalLevel,
                                member.classes.map { clazz ->
                                    Class(
                                        clazz.name,
                                        clazz.level
                                    )
                                },
                                Location(
                                    member.location.name,
                                    member.location.region,
                                    member.location.hexId,
                                    member.location.publicSpace
                                ),
                                member.guild,
                                member.homeServer
                            )
                        },
                        Instant.now()
                    )
                }
            }

//            parties.forEach { party ->
//                Timber.v("Parties server=${party.server} name=${party.quest?.name} area=${party.quest?.adventureArea} comment=${party.comment} updated=${party.updated}")
//            }

            val errorParty =
                parties.find { it.leader.name == "DDO Audit" && it.server != "Hardcore" }
            if (errorParty != null) {
                networkState.value = NetworkState.NotAvailable(errorParty.comment ?: "")
                return emptyList()
            }

            val nonAuditParties = parties.filter {
                it.leader.name != "DDO Audit" && it.server != "Hardcore"
            }

            withContext(Dispatchers.IO) {
                val partyIds = nonAuditParties.map { it.id }

                db.partyDao().deleteAllByNotPartyId(partyIds)
                db.partyDao().insertAll(nonAuditParties)
            }

            networkState.value = NetworkState.None

            return withContext(Dispatchers.IO) {
                db.partyDao().getAll()
            }
        } catch (e: Exception) {
            Timber.e(e)

            networkState.value = NetworkState.None
        }

        return emptyList()
    }

}