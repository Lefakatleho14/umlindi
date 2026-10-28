package com.project.umlindi.util

object AlertRules {

    fun radiusMultiplier(category: String): Double = when (category) {
        "break_in" -> 2.0
        "suspicious_activity" -> 1.0
        else -> 0.5
    }

    fun effectiveRadiusKm(category: String, userRadiusKm: Double): Double =
        userRadiusKm * radiusMultiplier(category)

    fun shouldNotify(category: String, distanceKm: Double?, userRadiusKm: Double): Boolean {
        if (distanceKm == null) return true
        return distanceKm <= effectiveRadiusKm(category, userRadiusKm)
    }
}