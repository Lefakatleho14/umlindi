package com.project.umlindi.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.project.umlindi.R
import com.project.umlindi.util.Validators

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val tvError = view.findViewById<TextView>(R.id.tvError)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvGoToRegister = view.findViewById<TextView>(R.id.tvGoToRegister)

        tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        btnLogin.setOnClickListener {
            tvError.visibility = View.GONE
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString().orEmpty()

            if (!Validators.isValidEmail(email)) {
                showError(tvError, getString(R.string.error_invalid_email))
                return@setOnClickListener
            }
            if (!Validators.isValidPassword(password)) {
                showError(tvError, getString(R.string.error_short_password))
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    if (task.isSuccessful) {
                        Log.d(TAG, "Login succeeded")
                        findNavController().navigate(R.id.action_login_to_home)
                    } else {
                        Log.e(TAG, "Login failed", task.exception)
                        showError(
                            tvError,
                            task.exception?.localizedMessage ?: getString(R.string.error_login_failed)
                        )
                    }
                }
        }
    }

    private fun showError(tvError: TextView, message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "LoginFragment"
    }
}