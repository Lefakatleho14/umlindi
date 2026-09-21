package com.project.umlindi.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test
    fun `valid email addresses are accepted`() {
        assertTrue(Validators.isValidEmail("user@example.com"))
        assertTrue(Validators.isValidEmail("lefa.motsoeneng@rosebank.co.za"))
    }

    @Test
    fun `invalid email addresses are rejected`() {
        assertFalse(Validators.isValidEmail("not-an-email"))
        assertFalse(Validators.isValidEmail("missing@domain"))
        assertFalse(Validators.isValidEmail(""))
    }

    @Test
    fun `password of 6 or more characters is valid`() {
        assertTrue(Validators.isValidPassword("123456"))
        assertTrue(Validators.isValidPassword("longerpassword"))
    }

    @Test
    fun `password shorter than 6 characters is invalid`() {
        assertFalse(Validators.isValidPassword("123"))
        assertFalse(Validators.isValidPassword(""))
    }

    @Test
    fun `matching passwords are detected`() {
        assertTrue(Validators.passwordsMatch("secret1", "secret1"))
        assertFalse(Validators.passwordsMatch("secret1", "secret2"))
    }

    @Test
    fun `display name cannot be blank`() {
        assertTrue(Validators.isValidDisplayName("Lindiwe"))
        assertFalse(Validators.isValidDisplayName("   "))
        assertFalse(Validators.isValidDisplayName(""))
    }

    @Test
    fun `description must be non-empty and under 500 characters`() {
        assertTrue(Validators.isValidDescription("Saw someone at the gate."))
        assertFalse(Validators.isValidDescription(""))
        assertFalse(Validators.isValidDescription("a".repeat(501)))
        assertTrue(Validators.isValidDescription("a".repeat(500)))
    }

    @Test
    fun `alert radius must be a positive number`() {
        assertTrue(Validators.isValidAlertRadius("5"))
        assertTrue(Validators.isValidAlertRadius("2.5"))
        assertFalse(Validators.isValidAlertRadius("0"))
        assertFalse(Validators.isValidAlertRadius("-3"))
        assertFalse(Validators.isValidAlertRadius("abc"))
    }
}