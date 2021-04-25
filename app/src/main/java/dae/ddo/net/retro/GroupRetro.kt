package dae.ddo.net.retro

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ServerGroupRetro(
    @SerializedName("Name")
    val name: String,
//    @SerializedName("LastUpdateTime")
//    val instant: Instant,
    @SerializedName("GroupCount")
    val groupCount: Int,
    @SerializedName("Groups")
    val parties: List<PartyRetro>
)

@Keep
data class PartyRetro(
    @SerializedName("Id")
    val id: Long,
    @SerializedName("Comment")
    val comment: String?,
    @SerializedName("Quest")
    val quest: PartyQuestRetro?,
    @SerializedName("Difficulty")
    val difficulty: String?,
    @SerializedName("AcceptedClasses")
    val acceptedClasses: List<String>?,
    @SerializedName("MinimumLevel")
    val minimumLevel: Int,
    @SerializedName("MaximumLevel")
    val maximumLevel: Int,
    @SerializedName("AdventureActive")
    val adventureActive: Int,
    @SerializedName("Leader")
    val leader: PlayerRetro,
    @SerializedName("Members")
    val members: List<PlayerRetro>,
)

@Keep
data class PartyQuestRetro(
    @SerializedName("HexId")
    val hexId: String,
    @SerializedName("Name")
    val name: String,
    @SerializedName("HeroicNormalCR")
    val heroicNormalCR: Int,
    @SerializedName("EpicNormalCR")
    val epicNormalCR: Int,
    @SerializedName("HeroicNormalXp")
    val heroicNormalXp: Int,
    @SerializedName("HeroicHardXp")
    val heroicHardXp: Int,
    @SerializedName("HeroicEliteXp")
    val heroicEliteXp: Int,
    @SerializedName("EpicNormalXp")
    val epicNormalXp: Int,
    @SerializedName("EpicHardXp")
    val epicHardXp: Int,
    @SerializedName("EpicEliteXp")
    val epicEliteXp: Int,
    @SerializedName("IsFreeToVip")
    val isFreeToVip: Boolean,
    @SerializedName("RequiredAdventurePack")
    val requiredAdventurePack: String?,
    @SerializedName("AdventureArea")
    val adventureArea: String?,
    @SerializedName("QuestJournalGroup")
    val questJournalGroup: String?,
    @SerializedName("GroupSize")
    val groupSize: String?,
    @SerializedName("Patron")
    val patron: String?,
)

@Keep
data class PlayerRetro(
    @SerializedName("Name")
    val name: String,
    @SerializedName("Gender")
    val gender: String,
    @SerializedName("Race")
    val race: String,
    @SerializedName("TotalLevel")
    val totalLevel: Int,
    @SerializedName("Classes")
    val classes: List<ClassRetro>,
    @SerializedName("Location")
    val location: LocationRetro,
    @SerializedName("Guild")
    val guild: String?,
    @SerializedName("HomeServer")
    val homeServer: String?
)

@Keep
data class LocationRetro(
    @SerializedName("Name")
    val name: String,
    @SerializedName("Region")
    val region: String?,
    @SerializedName("HexId")
    val hexId: String?,
    @SerializedName("IsPublicSpace")
    val publicSpace: Boolean
)

@Keep
data class ClassRetro(
    @SerializedName("Name")
    val name: String,
    @SerializedName("Level")
    val level: Int
)
