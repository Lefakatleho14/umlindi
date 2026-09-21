package com.project.umlindi.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.project.umlindi.R
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView

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

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tvError.text = "Please enter a valid email address."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (password.length < 6) {
                tvError.text = "Password must be at least 6 characters."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    if (task.isSuccessful) {
                        findNavController().navigate(R.id.action_login_to_home)
                    } else {
                        tvError.text = task.exception?.localizedMessage
                            ?: "Login failed. Please try again."
                        tvError.visibility = View.VISIBLE
                    }
                }
        }
    }
}