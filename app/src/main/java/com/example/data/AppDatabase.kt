package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Database(
    entities = [
        FoodBillEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodBillDao(): FoodBillDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE food_bills ADD COLUMN centerName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE food_bills ADD COLUMN subtitle TEXT NOT NULL DEFAULT ''")
            }
        }

        // Version 3 briefly added a "Medical Work" feature (now removed) with its own
        // tables; it made no changes to food_bills itself, so users still on version 2
        // can jump straight to version 4 with no schema change needed.
        private val MIGRATION_2_4 = object : Migration(2, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // No food_bills schema change between v2 and v4.
            }
        }

        // Users who briefly installed the version-3 build will have the now-unused
        // Medical tables on disk; drop them cleanly instead of leaving orphaned tables
        // or wiping the whole database via fallbackToDestructiveMigration.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS medical_records")
                db.execSQL("DROP TABLE IF EXISTS code_groups")
                db.execSQL("DROP TABLE IF EXISTS code_group_items")
                db.execSQL("DROP TABLE IF EXISTS preset_medical_codes")
            }
        }

        // Adds the "ধরন" (bill type) field so a bill remembers whether it's a market
        // list or a transport-fare memo, and re-opens with the matching form.
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE food_bills ADD COLUMN billType TEXT NOT NULL DEFAULT 'market'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "albaraka_food_bill_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_4, MIGRATION_3_4, MIGRATION_4_5)
                    // Safety net only: if some other unexpected version gap is hit,
                    // fall back to a clean database rather than crashing on launch.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
