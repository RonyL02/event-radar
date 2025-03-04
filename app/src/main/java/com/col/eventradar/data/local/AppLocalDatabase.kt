package com.col.eventradar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.col.eventradar.data.local.migrations.MIGRATION_1_2
import com.col.eventradar.models.EventEntity

@Database(entities = [EventEntity::class], version = 2, exportSchema = false)
@TypeConverters(LocalDateTimeConverter::class)
abstract class AppLocalDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var dbInstance: AppLocalDatabase? = null

        fun getDatabase(context: Context): AppLocalDatabase =
            dbInstance ?: synchronized(this) {
                val instance =
                    Room
                        .databaseBuilder(
                            context.applicationContext,
                            AppLocalDatabase::class.java,
                            "event_radar_database",
                        ).addMigrations(MIGRATION_1_2)
                        .build()
                dbInstance = instance
                instance
            }
    }
}
