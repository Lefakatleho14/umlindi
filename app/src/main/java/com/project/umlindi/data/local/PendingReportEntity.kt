package com.project.umlindi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A report that was made offline and is waiting to be uploaded. The location is
 * stored as two numbers because Room cannot store a Firestore GeoPoint directly
 * (Android Developers, n.d.b).
 */
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