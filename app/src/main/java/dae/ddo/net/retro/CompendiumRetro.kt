package dae.ddo.net.retro

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime

@Keep
data class CompendiumRetro(
    @SerializedName("ServerName")
    val name: String,
    @SerializedName("Entries")
    val quests: List<QuestRetro>
)

@Keep
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

@Keep
@Suppress("unused")
data class OccurrenceRetro(
    @SerializedName("Day")
    val instant: LocalDateTime,
    @SerializedName("Count")
    val count: Int,
)
