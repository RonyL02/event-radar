package com.col.eventradar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.col.eventradar.models.AreaEntity
import com.col.eventradar.models.EventEntity

@Database(entities = [EventEntity::class, AreaEntity::class], version = 2, exportSchema = false)
@TypeConverters(LocalDateTimeConverter::class)
abstract class AppLocalDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun areasDao(): AreasOfInterestDao

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
                        ).build()
                dbInstance = instance
                instance
            }
    }
}
