package com.project.umlindi.ui.auth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.project.umlindi.R
import com.project.umlindi.util.Validators

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var homeLocation: GeoPoint? = null

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchCurrentLocation()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etDisplayName = view.findViewById<TextInputEditText>(R.id.etDisplayName)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = view.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnUseLocation = view.findViewById<View>(R.id.btnUseLocation)
        val tvLocationStatus = view.findViewById<TextView>(R.id.tvLocationStatus)
        val tvError = view.findViewById<TextView>(R.id.tvError)
        val btnCreateAccount = view.findViewById<Button>(R.id.btnCreateAccount)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvGoToLogin = view.findViewById<TextView>(R.id.tvGoToLogin)

        tvGoToLogin.setOnClickListener { findNavController().popBackStack() }

        view.findViewById<View>(R.id.btnBack).setOnClickListener { findNavController().popBackStack() }

        btnUseLocation.setOnClickListener {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        btnCreateAccount.setOnClickListener {
            tvError.visibility = View.GONE

            val displayName = etDisplayName.text?.toString()?.trim().orEmpty()
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString().orEmpty()
            val confirmPassword = etConfirmPassword.text?.toString().orEmpty()

            if (!Validators.isValidDisplayName(displayName)) {
                showError(tvError, getString(R.string.error_display_name))
                return@setOnClickListener
            }
            if (!Validators.isValidEmail(email)) {
                showError(tvError, getString(R.string.error_invalid_email))
                return@setOnClickListener
            }
            if (!Validators.isValidPassword(password)) {
                showError(tvError, getString(R.string.error_short_password))
                return@setOnClickListener
            }
            if (!Validators.passwordsMatch(password, confirmPassword)) {
                showError(tvError, getString(R.string.error_passwords_mismatch))
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnCreateAccount.isEnabled = false

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        Log.d(TAG, "Account created")
                        val userDoc = hashMapOf(
                            "displayName" to displayName,
                            "email" to email,
                            "homeLocation" to homeLocation,
                            "alertRadiusKm" to 5.0,
                            "preferredLanguage" to "en",
                            "trustScore" to 0,
                            "createdAt" to Timestamp.now()
                        )
                        firestore.collection("users").document(uid).set(userDoc)
                            .addOnSuccessListener {
                                Log.d(TAG, "Profile saved")
                                progressBar.visibility = View.GONE
                                findNavController().navigate(R.id.action_register_to_home)
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Profile save failed", e)
                                progressBar.visibility = View.GONE
                                btnCreateAccount.isEnabled = true
                                showError(
                                    tvError,
                                    getString(R.string.error_profile_save_failed, e.localizedMessage ?: "")
                                )
                            }
                    } else {
                        Log.e(TAG, "Registration failed", task.exception)
                        progressBar.visibility = View.GONE
                        btnCreateAccount.isEnabled = true
                        showError(
                            tvError,
                            task.exception?.localizedMessage ?: getString(R.string.error_registration_failed)
                        )
                    }
                }
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            tvLocationStatus.text = getString(R.string.location_permission_granted)
        }
    }

    private fun fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val client = LocationServices.getFusedLocationProviderClient(requireContext())
        val statusView = view?.findViewById<TextView>(R.id.tvLocationStatus)

        client.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                applyLocation(location.latitude, location.longitude)
            } else {
                statusView?.text = getString(R.string.getting_location)
                val request = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .build()
                client.getCurrentLocation(request, null)
                    .addOnSuccessListener { freshLocation ->
                        if (freshLocation != null) {
                            applyLocation(freshLocation.latitude, freshLocation.longitude)
                        } else {
                            statusView?.text = getString(R.string.location_fetch_failed)
                        }
                    }
            }
        }
    }

    private fun applyLocation(lat: Double, lng: Double) {
        homeLocation = GeoPoint(lat, lng)
        view?.findViewById<TextView>(R.id.tvLocationStatus)?.text =
            getString(R.string.home_location_set, "%.4f".format(lat), "%.4f".format(lng))
    }

    private fun showError(tvError: TextView, message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "RegisterFragment"
    }
}