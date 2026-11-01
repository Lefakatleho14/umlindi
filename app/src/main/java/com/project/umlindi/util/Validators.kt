package com.project.umlindi.util

/**
 * Input validation shared by every screen. Keeping it in one place means each
 * rule is written once and covered by unit tests.
 */
object Validators {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    /** Six characters is also Firebase Authentication's minimum. */

    fun isValidPassword(password: String): Boolean = password.length >= 6

    fun passwordsMatch(password: String, confirmPassword: String): Boolean =
        password == confirmPassword

    fun isValidDisplayName(name: String): Boolean = name.trim().isNotEmpty()

    /** Report descriptions must not be empty and are capped at 500 characters. */

    fun isValidDescription(description: String): Boolean {
        val trimmed = description.trim()
        return trimmed.isNotEmpty() && trimmed.length <= 500
    }

    fun isValidAlertRadius(radiusText: String): Boolean {
        val radius = radiusText.trim().toDoubleOrNull()
        return radius != null && radius > 0
    }
}