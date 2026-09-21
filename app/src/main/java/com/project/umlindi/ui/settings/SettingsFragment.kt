package com.project.umlindi.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.project.umlindi.R

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val languageCodes = listOf("en", "zu", "st")
    private val languageLabels = listOf("English", "isiZulu", "Sesotho")

    private var pendingLocation: GeoPoint? = null

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchCurrentLocation()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etDisplayName = view.findViewById<TextInputEditText>(R.id.etDisplayName)
        val etAlertRadius = view.findViewById<TextInputEditText>(R.id.etAlertRadius)
        val spinnerLanguage = view.findViewById<Spinner>(R.id.spinnerLanguage)
        val btnUpdateLocation = view.findViewById<Button>(R.id.btnUpdateLocation)
        val tvLocationStatus = view.findViewById<TextView>(R.id.tvLocationStatus)
        val tvError = view.findViewById<TextView>(R.id.tvError)
        val tvSaved = view.findViewById<TextView>(R.id.tvSaved)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        spinnerLanguage.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, languageLabels
        )

        val uid = auth.currentUser?.uid
        if (uid == null) {
            findNavController().navigate(R.id.action_settings_to_login)
            return
        }

        // Load current settings
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                etDisplayName.setText(doc.getString("displayName") ?: "")
                val radius = doc.getDouble("alertRadiusKm") ?: 5.0
                etAlertRadius.setText(radius.toString())

                val lang = doc.getString("preferredLanguage") ?: "en"
                spinnerLanguage.setSelection(languageCodes.indexOf(lang).coerceAtLeast(0))

                val geo = doc.getGeoPoint("homeLocation")
                if (geo != null) {
                    tvLocationStatus.text = "Current: (${"%.4f".format(geo.latitude)}, ${"%.4f".format(geo.longitude)})"
                } else {
                    tvLocationStatus.text = "No home location set"
                }
            }
            .addOnFailureListener {
                tvError.text = "Could not load settings."
                tvError.visibility = View.VISIBLE
            }

        btnUpdateLocation.setOnClickListener {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        btnSave.setOnClickListener {
            tvError.visibility = View.GONE
            tvSaved.visibility = View.GONE

            val displayName = etDisplayName.text?.toString()?.trim().orEmpty()
            val radiusText = etAlertRadius.text?.toString()?.trim().orEmpty()
            val radius = radiusText.toDoubleOrNull()
            val language = languageCodes[spinnerLanguage.selectedItemPosition]

            if (displayName.isEmpty()) {
                showError(tvError, "Display name cannot be empty.")
                return@setOnClickListener
            }
            if (radius == null || radius <= 0) {
                showError(tvError, "Please enter a valid alert radius.")
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnSave.isEnabled = false

            val updates = mutableMapOf<String, Any>(
                "displayName" to displayName,
                "alertRadiusKm" to radius,
                "preferredLanguage" to language
            )
            pendingLocation?.let { updates["homeLocation"] = it }

            firestore.collection("users").document(uid).update(updates)
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    tvSaved.visibility = View.VISIBLE
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    showError(tvError, e.localizedMessage ?: "Failed to save settings.")
                }
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_settings_to_login)
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
                statusView?.text = "Getting location…"
                val request = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .build()
                client.getCurrentLocation(request, null)
                    .addOnSuccessListener { freshLocation ->
                        if (freshLocation != null) {
                            applyLocation(freshLocation.latitude, freshLocation.longitude)
                        } else {
                            statusView?.text = "Could not fetch location — check GPS is enabled"
                        }
                    }
            }
        }
    }

    private fun applyLocation(lat: Double, lng: Double) {
        pendingLocation = GeoPoint(lat, lng)
        view?.findViewById<TextView>(R.id.tvLocationStatus)?.text =
            "New location set (${"%.4f".format(lat)}, ${"%.4f".format(lng)}) — tap Save to confirm"
    }

    private fun showError(tvError: TextView, message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }
}