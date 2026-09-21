package com.project.umlindi.ui.report

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.project.umlindi.R

class ReportIncidentFragment : Fragment(R.layout.fragment_report_incident) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val categoryValues = listOf("break_in", "suspicious_activity", "load_shedding_risk", "other")
    private val categoryLabels = listOf("Break-in in progress", "Suspicious activity", "Load-shedding risk", "Other")

    private var reportLocation: GeoPoint? = null

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchCurrentLocation()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinnerCategory = view.findViewById<Spinner>(R.id.spinnerCategory)
        val etDescription = view.findViewById<TextInputEditText>(R.id.etDescription)
        val tvCharCount = view.findViewById<TextView>(R.id.tvCharCount)
        val btnUseLocation = view.findViewById<Button>(R.id.btnUseLocation)
        val tvError = view.findViewById<TextView>(R.id.tvError)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        spinnerCategory.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryLabels
        )

        etDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                tvCharCount.text = "${s?.length ?: 0}/500"
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnUseLocation.setOnClickListener {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        btnSubmit.setOnClickListener {
            tvError.visibility = View.GONE

            val description = etDescription.text?.toString()?.trim().orEmpty()
            val category = categoryValues[spinnerCategory.selectedItemPosition]
            val userId = auth.currentUser?.uid

            if (description.isEmpty()) {
                showError(tvError, "Please describe what happened.")
                return@setOnClickListener
            }
            if (reportLocation == null) {
                showError(tvError, "Please set a location for this report.")
                return@setOnClickListener
            }
            if (userId == null) {
                showError(tvError, "You must be logged in to submit a report.")
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnSubmit.isEnabled = false

            val report = hashMapOf(
                "reporterId" to userId,
                "category" to category,
                "description" to description,
                "location" to reportLocation,
                "status" to "pending",
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )

            firestore.collection("reports").add(report)
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    findNavController().popBackStack()
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    btnSubmit.isEnabled = true
                    showError(tvError, e.localizedMessage ?: "Failed to submit report.")
                }
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchCurrentLocation()
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
        reportLocation = GeoPoint(lat, lng)
        view?.findViewById<TextView>(R.id.tvLocationStatus)?.text =
            "Location set (${"%.4f".format(lat)}, ${"%.4f".format(lng)})"
    }

    private fun showError(tvError: TextView, message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }
}