package com.project.umlindi.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

/** Database access for the offline queue (Android Developers, n.d.b). */

@Dao
interface PendingReportDao {

    @Insert
    suspend fun insert(report: PendingReportEntity): Long

    /** Oldest first, so reports upload in the order they were made. */

    @Query("SELECT * FROM pending_reports ORDER BY createdAtMillis ASC")
    suspend fun getAll(): List<PendingReportEntity>

    @Delete
    suspend fun delete(report: PendingReportEntity)
}