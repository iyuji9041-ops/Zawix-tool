package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.NoteEntity
import com.example.data.model.ToolUsageEntity

@Database(
    entities = [NoteEntity::class, ToolUsageEntity::class],
    version = 1,
    exportSchema = true // Schema exported for automated schema validation and migrations
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun toolUsageDao(): ToolUsageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Explicit migration from version 1 to 2 to ensure user notes and tool usage are never wiped
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Future schema changes (e.g. adding columns or tables) are safely performed here
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zaswix_tools.db"
                )
                    // Disallow destructive migrations to protect user notes across schema updates
                    .addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
