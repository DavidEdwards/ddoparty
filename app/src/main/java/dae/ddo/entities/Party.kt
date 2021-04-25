package dae.ddo.entities

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.threeten.bp.Instant

@Keep
@Entity(
    indices = [Index("id", unique = true)]
)
data class Party(
    @PrimaryKey val id: Long,
    val server: String,
    val comment: String?,
    @Embedded(prefix = "party_quest_") val quest: PartyQuest?,
    val difficulty: String?,
//    val acceptedClasses: List<String>?,
    val minimumLevel: Int?,
    val maximumLevel: Int?,
    val adventureActive: Int?,
    @Embedded(prefix = "party_leader_") val leader: Player,
    val members: List<Player>?,
    val updated: Instant
) {
    fun players(): List<Player> {
        return (listOf(leader) + members.orEmpty())
    }

    companion object {
        fun sample(id: Int): Party {
            return Party(
                id = 0,
                server = "Server ${id % 10}",
                comment = "Comment $id",
                quest = PartyQuest.sample(id % 20),
                difficulty = "Difficulty $id",
                minimumLevel = (id % 25),
                maximumLevel = (id % 25) + 4,
                adventureActive = id * 2,
                leader = Player.sample((id * 1000) + 1, 1, id % 50),
                members = listOf(
                    Player.sample((id * 1000) + 2, 2, id % 50),
                    Player.sample((id * 1000) + 3, 3, id % 50),
                    Player.sample((id * 1000) + 4, 4, id % 50),
                    Player.sample((id * 1000) + 5, 5, id % 50)
                ),
                updated = Instant.now().minusMillis((0L..3600000L).random())
            )
        }
    }
}

@Keep
data class PartyQuest(
    val hexId: String,
    val name: String?,
    val heroicNormalCR: Int,
    val epicNormalCR: Int,
    val heroicNormalXp: Int,
    val heroicHardXp: Int,
    val heroicEliteXp: Int,
    val epicNormalXp: Int,
    val epicHardXp: Int,
    val epicEliteXp: Int,
    val isFreeToVip: Boolean,
    val requiredAdventurePack: String?,
    val adventureArea: String?,
    val questJournalGroup: String?,
    val groupSize: String?,
    val patron: String?,
) {
    companion object {
        fun sample(id: Int): PartyQuest {
            return PartyQuest(
                hexId = "hex$id",
                name = "Name $id",
                heroicNormalCR = id,
                epicNormalCR = id,
                heroicNormalXp = id,
                heroicHardXp = id,
                heroicEliteXp = id,
                epicNormalXp = id,
                epicHardXp = id,
                epicEliteXp = id,
                isFreeToVip = id % 3 == 0,
                requiredAdventurePack = "Pack $id",
                adventureArea = "Area $id",
                questJournalGroup = "Journal $id",
                groupSize = if (id % 2 == 0) "Party" else "Raid",
                patron = "Patron $id"
            )
        }
    }
}

@Keep
data class Player(
    val name: String,
    val gender: String,
    val race: String,
    val totalLevel: Int,
    val classes: List<Class>,
    @Embedded(prefix = "player_location_") val location: Location,
    val guild: String?,
    val homeServer: String?
) {
    companion object {
        fun sample(id: Int, classId: Int, locationId: Int): Player {
            return Player(
                name = "Player $id",
                gender = if (id % 2 == 0) "Male" else "Female",
                race = "Race $id",
                totalLevel = 1,
                classes = listOf(
                    Class.sample(classId)
                ),
                location = Location.sample(locationId),
                guild = "Guild $id",
                homeServer = "Server $id"
            )
        }
    }
}

@Keep
data class Location(
    val name: String,
    val region: String?,
    val hexId: String?,
    val publicSpace: Boolean
) {
    companion object {
        fun sample(id: Int): Location {
            return Location(
                name = "Name $id",
                region = "Region $id",
                hexId = "hex$id",
                publicSpace = id % 2 == 0
            )
        }
    }
}

@Keep
data class Class(
    val name: String,
    val level: Int
) {
    companion object {
        fun sample(id: Int): Class {
            return Class(
                name = when (id % 5) {
                    0 -> "Rogue"
                    1 -> "Fighter"
                    2 -> "Ranger"
                    3 -> "Paladin"
                    4 -> "Cleric"
                    else -> "Druid"
                },
                level = id % 30
            )
        }
    }
}