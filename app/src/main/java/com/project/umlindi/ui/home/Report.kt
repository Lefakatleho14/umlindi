package com.project.umlindi.ui.home

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.Timestamp

data class Report(
    val reportId: String = "",
    val reporterId: String = "",
    val category: String = "",
    val description: String = "",
    val location: GeoPoint? = null,
    val status: String = "pending",
    val createdAt: Timestamp? = null
)