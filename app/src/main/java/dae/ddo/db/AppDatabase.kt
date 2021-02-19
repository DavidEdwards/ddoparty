package dae.ddo.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dae.ddo.db.dao.FilterDao
import dae.ddo.db.dao.PartyDao
import dae.ddo.db.dao.QuestDao
import dae.ddo.entities.*
import org.threeten.bp.Instant
import java.lang.reflect.Type


@Database(
    entities = [Quest::class, Party::class, Filter::class, Condition::class],
    version = 16
)
@TypeConverters(
    PlayerListConverter::class,
    ClassListConverter::class,
    InstantConverter::class,
    ConditionTypeConverter::class
)
abstract class BaseDatabase : RoomDatabase() {
    abstract fun questDao(): QuestDao
    abstract fun partyDao(): PartyDao
    abstract fun filterDao(): FilterDao
}

class PlayerListConverter {
    @TypeConverter
    fun listToJson(list: List<Player?>?): String? {
        if (list == null) return null
        val type: Type = object : TypeToken<List<Player?>?>() {}.type
        val json: String = Gson().toJson(list, type)
        return if (list.isEmpty()) null else json
    }

    @TypeConverter
    fun jsonToList(json: String?): List<Player> {
        if (json == null) return emptyList()
        val gson = Gson()
        val type: Type = object : TypeToken<List<Player?>?>() {}.type
        return gson.fromJson(json, type)
    }
}

class ClassListConverter {
    @TypeConverter
    fun listToJson(list: List<Class?>?): String? {
        if (list == null) return null
        val type: Type = object : TypeToken<List<Class?>?>() {}.type
        val json: String = Gson().toJson(list, type)
        return if (list.isEmpty()) null else json
    }

    @TypeConverter
    fun jsonToList(json: String?): List<Class> {
        if (json == null) return emptyList()
        val gson = Gson()
        val type: Type = object : TypeToken<List<Class?>?>() {}.type
        return gson.fromJson(json, type)
    }
}

class InstantConverter {
    @TypeConverter
    fun instantToLong(instant: Instant): Long {
        return instant.toEpochMilli()
    }

    @TypeConverter
    fun longToInstant(millis: Long): Instant {
        return Instant.ofEpochMilli(millis)
    }
}

class ConditionTypeConverter {
    @TypeConverter
    fun conditionTypeToString(item: ConditionType): String {
        return item.name
    }

    @TypeConverter
    fun stringToConditionType(item: String): ConditionType {
        return ConditionType.valueOf(item)
    }
}