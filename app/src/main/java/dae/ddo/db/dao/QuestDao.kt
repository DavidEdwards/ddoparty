package dae.ddo.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import dae.ddo.entities.Quest
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT count(*) FROM quest")
    fun count(): Int

    @Query("SELECT count(*) FROM quest")
    fun flowCount(): Flow<Int>

    @Query("SELECT * FROM quest")
    fun getAll(): List<Quest>

    @Query("SELECT * FROM quest")
    fun flowAll(): Flow<List<Quest>>

    @Query("SELECT * FROM quest")
    fun liveAll(): LiveData<List<Quest>>

    @Query("SELECT * FROM quest WHERE id IN (:ids)")
    fun loadAllByIds(ids: IntArray): List<Quest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg entities: Quest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entities: List<Quest>)

    @Delete
    fun delete(entity: Quest)
}