package com.col.eventradar.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 1. Rename old table
            database.execSQL("ALTER TABLE events RENAME TO events_old")

            // 2. Create new table with correct schema
            database.execSQL(
                """
                CREATE TABLE events (
                    id TEXT PRIMARY KEY NOT NULL,
                    title TEXT NOT NULL,
                    locationName TEXT NOT NULL,
                    time TEXT NOT NULL, -- Store as String first to avoid data loss
                    type TEXT NOT NULL,
                    description TEXT NOT NULL
                )
                """.trimIndent(),
            )

            // 3. Copy data from old table (converting `time` format if needed)
            database.execSQL(
                """
                INSERT INTO events (id, title, locationName, time, type, description)
                SELECT id, title, locationName, time, type, description FROM events_old
                """.trimIndent(),
            )

            // 4. Drop the old table
            database.execSQL("DROP TABLE events_old")
        }
    }
