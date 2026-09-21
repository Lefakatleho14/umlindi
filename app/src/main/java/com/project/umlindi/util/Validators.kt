package com.project.umlindi.util

object Validators {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    fun isValidPassword(password: String): Boolean = password.length >= 6

    fun passwordsMatch(password: String, confirmPassword: String): Boolean =
        password == confirmPassword

    fun isValidDisplayName(name: String): Boolean = name.trim().isNotEmpty()

    fun isValidDescription(description: String): Boolean {
        val trimmed = description.trim()
        return trimmed.isNotEmpty() && trimmed.length <= 500
    }

    fun isValidAlertRadius(radiusText: String): Boolean {
        val radius = radiusText.trim().toDoubleOrNull()
        return radius != null && radius > 0
    }
}