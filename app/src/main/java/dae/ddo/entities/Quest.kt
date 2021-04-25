package dae.ddo.entities

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Keep
@Entity(
    indices = [Index("name", unique = true)]
)
data class Quest(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val patron: String?,
    val heroicCR: Int,
    val epicCR: Int?,
    val raid: Boolean,
) {
    companion object {
        fun sample(id: Int): Quest {
            return Quest(
                id = id,
                name = "Quest $id",
                patron = "Patron $id",
                heroicCR = id % 30,
                epicCR = (id * 10) % 30,
                raid = id % 2 == 0
            )
        }
    }
}