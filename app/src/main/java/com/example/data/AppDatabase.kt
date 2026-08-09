package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Database(entities = [FoodBillEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodBillDao(): FoodBillDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Adds the centerName/subtitle columns introduced in version 2 without touching
        // any existing rows, so previously saved bills are preserved across the update
        // instead of being wiped by a destructive fallback.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE food_bills ADD COLUMN centerName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE food_bills ADD COLUMN subtitle TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "albaraka_food_bill_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    // Safety net only: if a future version bump has no matching migration,
                    // fall back to a clean database rather than crashing on launch.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
