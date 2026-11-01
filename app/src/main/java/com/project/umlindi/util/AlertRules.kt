package com.project.umlindi.util

/**
 * Decides whether a new report should notify a user. This is the app's
 * "severity-aware alert radius" feature: serious incidents reach further
 * than minor ones, so users are not flooded with distant low-priority alerts.
 */
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