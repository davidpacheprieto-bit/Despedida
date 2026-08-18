package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CheckpointEntity::class,
        PartyNotificationEntity::class,
        GameSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PartyDatabase : RoomDatabase() {
    abstract fun partyDao(): PartyDao

    companion object {
        @Volatile
        private var INSTANCE: PartyDatabase? = null

        fun getDatabase(context: Context): PartyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PartyDatabase::class.java,
                    "despedida_leon_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
