package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Database(
    entities = [
        FoodBillEntity::class,
        MedicalRecordEntity::class,
        CodeGroupEntity::class,
        CodeGroupItemEntity::class,
        PresetMedicalCodeEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodBillDao(): FoodBillDao
    abstract fun medicalDao(): MedicalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE food_bills ADD COLUMN centerName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE food_bills ADD COLUMN subtitle TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `medical_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `patientId` TEXT NOT NULL, `code` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `notes` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `code_groups` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `groupName` TEXT NOT NULL, `description` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `code_group_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `groupId` INTEGER NOT NULL, `code` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `preset_medical_codes` (`code` TEXT PRIMARY KEY NOT NULL, `name` TEXT NOT NULL, `category` TEXT NOT NULL)")
            }
        }

        // Enforces ONE CODE = ONE OWNER: rebuilds code_group_items with a unique,
        // case-insensitive index on `code`. If old data already had the same code
        // under more than one owner (previously allowed), we keep only the earliest
        // assignment (lowest id) for that code and drop the later duplicates so
        // existing records are preserved instead of destroyed.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `code_group_items_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`groupId` INTEGER NOT NULL, " +
                        "`code` TEXT NOT NULL COLLATE NOCASE)"
                )
                db.execSQL(
                    "INSERT INTO code_group_items_new (id, groupId, code) " +
                        "SELECT id, groupId, code FROM code_group_items " +
                        "WHERE id IN (SELECT MIN(id) FROM code_group_items GROUP BY code COLLATE NOCASE)"
                )
                db.execSQL("DROP TABLE code_group_items")
                db.execSQL("ALTER TABLE code_group_items_new RENAME TO code_group_items")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_code_group_items_code` " +
                        "ON `code_group_items` (`code`)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "albaraka_food_bill_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
