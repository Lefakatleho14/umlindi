package com.project.umlindi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** The local Room database. It only holds the offline report queue (Android Developers, n.d.b). */

@Database(entities = [PendingReportEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pendingReportDao(): PendingReportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Singleton, because opening several instances of one Room database wastes resources. */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "umlindi_db"
                ).build().also { INSTANCE = it }
            }
    }
}