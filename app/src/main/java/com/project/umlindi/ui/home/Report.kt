package com.project.umlindi.ui.home

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.Timestamp


/**
 * A report as stored in the Firestore "reports" collection. Every field has a
 * default value, because Firestore needs a no-argument constructor to convert
 * documents into this class.
 */
data class Report(
    val reportId: String = "",
    val reporterId: String = "",
    val category: String = "",
    val description: String = "",
    val location: GeoPoint? = null,
    val status: String = "pending",
    val createdAt: Timestamp? = null
)