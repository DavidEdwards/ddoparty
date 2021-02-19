package dae.ddo.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import dae.ddo.entities.Condition
import dae.ddo.entities.Filter
import dae.ddo.entities.FilterConditions
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

@Dao
interface FilterDao {
    @Query("SELECT count(*) FROM filter")
    fun count(): Int

    @Query("SELECT * FROM filter")
    fun getAll(): List<Filter>

    @Query("SELECT * FROM filter")
    fun flowAll(): Flow<List<Filter>>

    @Query("SELECT * FROM filter")
    fun flowAllFilterConditions(): Flow<List<FilterConditions>>

    @Query("SELECT * FROM filter")
    fun getAllFilterConditions(): List<FilterConditions>

    @Query("SELECT * FROM filter WHERE id = :filterId LIMIT 1")
    fun getFilterConditions(filterId: Long): Flow<FilterConditions>

    @Query("SELECT * FROM condition WHERE id = :conditionId LIMIT 1")
    fun getCondition(conditionId: Long): Flow<Condition>

    @Query("SELECT * FROM filter")
    fun liveAll(): LiveData<List<Filter>>

    @Query("SELECT * FROM condition WHERE id = :filterId")
    fun flowAllConditions(filterId: Long): Flow<List<Condition>>

    @Query("SELECT * FROM condition WHERE id = :filterId")
    fun liveAllConditions(filterId: Long): LiveData<List<Condition>>

    @Query("SELECT * FROM filter WHERE id IN (:ids)")
    fun loadAllByIds(ids: LongArray): List<Filter>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg entities: Filter): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entities: List<Filter>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllConditions(vararg entities: Condition): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllConditions(entities: List<Condition>): List<Long>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: FilterConditions): Long? {
        Timber.v("Insert: $entity")

        val filterId = insertAll(entity.filter).firstOrNull() ?: return null
        insertAllConditions(entity.conditions.map {
            Condition(it.id, filterId, it.type, it.arg1, it.arg2, it.arg3, it.arg4, it.arg5)
        })
        return filterId
    }

    @Delete
    fun delete(entity: Filter)

    @Query("DELETE FROM filter WHERE id = :id")
    fun deleteFilterById(id: Long)

    @Query("DELETE FROM condition WHERE id = :id")
    fun deleteConditionById(id: Long)
}