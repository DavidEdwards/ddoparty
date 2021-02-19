package dae.ddo.net.retro

import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime

data class CompendiumRetro(
    @SerializedName("ServerName")
    val name: String,
    @SerializedName("Entries")
    val quests: List<QuestRetro>
)

data class QuestRetro(
    @SerializedName("QuestName")
    val name: String,
    @SerializedName("Patron")
    val patron: String?,
//    @SerializedName("Occurrences")
//    val occurrences: List<OccurrenceRetro>,
    @SerializedName("CR_Heroic")
    val heroicCR: Int,
    @SerializedName("CR_Epic")
    val epicCR: Int?,
    @SerializedName("Raid")
    val raid: Boolean,
)

data class OccurrenceRetro(
    @SerializedName("Day")
    val instant: LocalDateTime,
    @SerializedName("Count")
    val count: Int,
)
