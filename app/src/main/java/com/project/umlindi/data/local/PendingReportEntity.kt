package com.project.umlindi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_reports")
data class PendingReportEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val reporterId: String,
    val category: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val createdAtMillis: Long
)