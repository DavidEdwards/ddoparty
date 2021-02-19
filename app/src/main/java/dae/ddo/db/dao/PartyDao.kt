package dae.ddo.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import dae.ddo.entities.Party
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Query("SELECT count(*) FROM party")
    fun count(): Int

    @Query("SELECT * FROM party")
    fun getAll(): List<Party>

    @Query("SELECT * FROM party")
    fun flowAll(): Flow<List<Party>>

    @Query("SELECT * FROM party")
    fun liveAll(): LiveData<List<Party>>

    @Query("SELECT * FROM party WHERE id IN (:ids)")
    fun loadAllByIds(ids: IntArray): List<Party>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg entities: Party)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entities: List<Party>)

    @Query("DELETE FROM party WHERE id IN (:ids)")
    fun deleteAllByPartyId(ids: List<Long>)

    @Query("DELETE FROM party WHERE id NOT IN (:ids)")
    fun deleteAllByNotPartyId(ids: List<Long>)

    @Delete
    fun delete(entity: Party)
}