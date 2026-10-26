package com.project.umlindi.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PendingReportDao {

    @Insert
    suspend fun insert(report: PendingReportEntity): Long

    @Query("SELECT * FROM pending_reports ORDER BY createdAtMillis ASC")
    suspend fun getAll(): List<PendingReportEntity>

    @Delete
    suspend fun delete(report: PendingReportEntity)
}