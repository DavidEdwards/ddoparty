package dae.ddo.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dae.ddo.db.BaseDatabase
import dae.ddo.db.dao.QuestDao
import dae.ddo.repositories.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@InstallIn(SingletonComponent::class)
@Module
class AppModule {
    @Provides
    fun provideQuestDao(db: BaseDatabase): QuestDao {
        return db.questDao()
    }

    @Provides
    @Singleton
    fun provideBaseDatabase(@ApplicationContext appContext: Context): BaseDatabase {
        return Room.databaseBuilder(
            appContext,
            BaseDatabase::class.java,
            "main"
        )
            .fallbackToDestructiveMigration()
            .addMigrations(
                object : Migration(15, 16) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("ALTER TABLE filter ADD COLUMN enabled INT NOT NULL DEFAULT 1")
                    }
                }
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideMainRepository(
        @ApplicationContext appContext: Context,
        baseDatabase: BaseDatabase
    ): MainRepository {
        return MainRepository(
            baseDatabase,
            appContext.dataStore,
            appContext.assets
        )
    }

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext appContext: Context
    ): DataStore<Preferences> {
        return appContext.dataStore
    }
}