package dae.ddo.entities

import androidx.annotation.StringRes
import androidx.room.*
import dae.ddo.R
import dae.ddo.utils.extensions.orElse

@Entity
data class Filter(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val mustMatch: Boolean,
    val relevancy: Int,
    val enabled: Boolean
)

enum class ConditionType(
    @StringRes
    val readable: Int
) {
    NONE(R.string.none),
    SERVER(R.string.server),
    GUILD(R.string.guild),
    PLAYER(R.string.player),
    LEVEL(R.string.level_range),
    QUEST(R.string.quest_name),
    RAID(R.string.is_a_raid),
    DIFFICULTY(R.string.difficulty)
}

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Filter::class,
            parentColumns = ["id"],
            childColumns = ["filterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("filterId", unique = true)]
)
data class Condition(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val filterId: Long,
    val type: ConditionType,
    val arg1: String,
    val arg2: Int,
    val arg3: Int,
    val arg4: Float,
    val arg5: Boolean
) {
    fun match(party: Party): Boolean {
        return when (type) {
            ConditionType.NONE -> false
            ConditionType.SERVER -> party.server.contains(arg1.toRegex(RegexOption.IGNORE_CASE))
            ConditionType.GUILD -> party.players()
                .any { it.guild?.contains(arg1.toRegex(RegexOption.IGNORE_CASE)) == true }
            ConditionType.PLAYER -> party.players()
                .any { it.name.contains(arg1.toRegex(RegexOption.IGNORE_CASE)) }
            ConditionType.LEVEL -> {
                val range1 =
                    ((party.minimumLevel ?: Int.MIN_VALUE)..(party.maximumLevel ?: Int.MAX_VALUE))
                val range2 =
                    ((arg2)..(arg3))

                range1.contains(arg2) || range1.contains(arg3) ||
                        range2.contains(party.minimumLevel) || range2.contains(party.maximumLevel)
            }
            ConditionType.QUEST -> party.quest?.name?.contains(arg1.toRegex(RegexOption.IGNORE_CASE))
                .orElse(false) ||
                    party.quest?.adventureArea?.contains(arg1.toRegex(RegexOption.IGNORE_CASE))
                        .orElse(false)
            ConditionType.RAID -> (party.quest?.groupSize == "Raid") == arg5
            ConditionType.DIFFICULTY -> party.difficulty?.contains(arg1.toRegex(RegexOption.IGNORE_CASE)) == true
        }
    }

    companion object {
        fun dummy(): Condition =
            Condition(0, 0, ConditionType.NONE, "", 0, 0, 0f, false)
    }
}

data class FilterConditions(
    @Embedded
    val filter: Filter,
    @Relation(
        entity = Condition::class,
        parentColumn = "id",
        entityColumn = "filterId"
    ) val conditions: List<Condition>
) {
    fun match(party: Party): Boolean {
        return conditions.all { it.match(party) }
    }

    companion object {
        fun dummy(): FilterConditions =
            FilterConditions(Filter(0, "", false, 0, true), listOf())

        fun quest(quest: String) =
            FilterConditions(
                Filter(0, "Quest", false, 2, true), listOf(
                    Condition(0, 0, ConditionType.QUEST, quest, 0, 0, 0f, false)
                )
            )

        fun patron(patron: String) =
            FilterConditions(
                Filter(0, "Patron", false, 2, true), listOf(
                    Condition(0, 0, ConditionType.QUEST, patron, 0, 0, 0f, false)
                )
            )

        fun area(area: String) =
            FilterConditions(
                Filter(0, "Area", false, 2, true), listOf(
                    Condition(0, 0, ConditionType.QUEST, area, 0, 0, 0f, false)
                )
            )

        fun guild(guild: String) =
            FilterConditions(
                Filter(0, "Guild", false, 2, true), listOf(
                    Condition(0, 0, ConditionType.GUILD, guild, 0, 0, 0f, false)
                )
            )

        fun server(server: String) =
            FilterConditions(
                Filter(0, "Server", false, 4, true), listOf(
                    Condition(0, 0, ConditionType.SERVER, server, 0, 0, 0f, false)
                )
            )
    }
}

fun List<FilterConditions>.match(party: Party): Boolean {
    return all { it.match(party) }
}