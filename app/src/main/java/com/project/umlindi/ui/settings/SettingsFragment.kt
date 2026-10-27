package com.project.umlindi.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
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
import com.project.umlindi.util.Validators

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

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                etDisplayName.setText(doc.getString("displayName") ?: "")
                etAlertRadius.setText((doc.getDouble("alertRadiusKm") ?: 5.0).toString())

                val lang = doc.getString("preferredLanguage") ?: "en"
                spinnerLanguage.setSelection(languageCodes.indexOf(lang).coerceAtLeast(0))

                val geo = doc.getGeoPoint("homeLocation")
                tvLocationStatus.text = if (geo != null) {
                    getString(
                        R.string.settings_current_location,
                        "%.4f".format(geo.latitude),
                        "%.4f".format(geo.longitude)
                    )
                } else {
                    getString(R.string.settings_no_home_location)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Loading settings failed", e)
                showError(tvError, getString(R.string.error_settings_load))
            }

        btnUpdateLocation.setOnClickListener {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        btnSave.setOnClickListener {
            tvError.visibility = View.GONE
            tvSaved.visibility = View.GONE

            val displayName = etDisplayName.text?.toString()?.trim().orEmpty()
            val radiusText = etAlertRadius.text?.toString()?.trim().orEmpty()
            val language = languageCodes[spinnerLanguage.selectedItemPosition]

            if (!Validators.isValidDisplayName(displayName)) {
                showError(tvError, getString(R.string.error_display_name_empty))
                return@setOnClickListener
            }
            if (!Validators.isValidAlertRadius(radiusText)) {
                showError(tvError, getString(R.string.error_radius_invalid))
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnSave.isEnabled = false

            val updates = mutableMapOf<String, Any>(
                "displayName" to displayName,
                "alertRadiusKm" to radiusText.toDouble(),
                "preferredLanguage" to language
            )
            pendingLocation?.let { updates["homeLocation"] = it }

            firestore.collection("users").document(uid).update(updates)
                .addOnSuccessListener {
                    Log.d(TAG, "Settings saved, language=$language")
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    tvSaved.visibility = View.VISIBLE
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Saving settings failed", e)
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    showError(tvError, e.localizedMessage ?: getString(R.string.error_settings_save))
                }
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_settings_to_login)
        }

        btnLogout.setOnClickListener {
            LoginManager.getInstance().logOut()
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
        pendingLocation = GeoPoint(lat, lng)
        view?.findViewById<TextView>(R.id.tvLocationStatus)?.text =
            getString(R.string.settings_new_location, "%.4f".format(lat), "%.4f".format(lng))
    }

    private fun showError(tvError: TextView, message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "SettingsFragment"
    }
}