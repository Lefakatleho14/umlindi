package com.project.umlindi.ui.report

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.project.umlindi.R
import com.project.umlindi.data.local.AppDatabase
import com.project.umlindi.data.local.PendingReportEntity
import com.project.umlindi.sync.ReportSyncWorker
import com.project.umlindi.util.NetworkUtils
import com.project.umlindi.util.Validators
import kotlinx.coroutines.launch

class ReportIncidentFragment : Fragment(R.layout.fragment_report_incident) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val categoryValues = listOf("break_in", "suspicious_activity", "load_shedding_risk", "other")
    private val categoryLabelRes = listOf(
        R.string.category_break_in,
        R.string.category_suspicious,
        R.string.category_load_shedding,
        R.string.category_other
    )

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
        val btnUseLocation = view.findViewById<View>(R.id.btnUseLocation)
        val tvError = view.findViewById<TextView>(R.id.tvError)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        spinnerCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categoryLabelRes.map { getString(it) }
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
            val location = reportLocation

            if (!Validators.isValidDescription(description)) {
                showError(tvError, getString(R.string.error_describe))
                return@setOnClickListener
            }
            if (location == null) {
                showError(tvError, getString(R.string.error_set_location))
                return@setOnClickListener
            }
            if (userId == null) {
                showError(tvError, getString(R.string.error_not_logged_in))
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnSubmit.isEnabled = false

            if (NetworkUtils.isOnline(requireContext())) {
                submitOnline(userId, category, description, location)
            } else {
                Log.d(TAG, "Offline, queueing report locally")
                saveReportOffline(userId, category, description, location)
            }
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchCurrentLocation()
        }
    }

    private fun submitOnline(
        userId: String,
        category: String,
        description: String,
        location: GeoPoint
    ) {
        val report = hashMapOf(
            "reporterId" to userId,
            "category" to category,
            "description" to description,
            "location" to location,
            "status" to "pending",
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
        )

        firestore.collection("reports").add(report)
            .addOnSuccessListener {
                Log.d(TAG, "Report submitted online")
                view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Online submit failed, queueing locally", e)
                saveReportOffline(userId, category, description, location)
            }
    }

    private fun saveReportOffline(
        userId: String,
        category: String,
        description: String,
        location: GeoPoint
    ) {
        val appContext = requireContext().applicationContext
        val savedMessage = getString(R.string.toast_saved_offline)

        viewLifecycleOwner.lifecycleScope.launch {
            AppDatabase.getInstance(appContext).pendingReportDao().insert(
                PendingReportEntity(
                    reporterId = userId,
                    category = category,
                    description = description,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    createdAtMillis = System.currentTimeMillis()
                )
            )

            val syncRequest = OneTimeWorkRequestBuilder<ReportSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(appContext).enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                syncRequest
            )

            view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
            Toast.makeText(appContext, savedMessage, Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
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
        reportLocation = GeoPoint(lat, lng)
        view?.findViewById<TextView>(R.id.tvLocationStatus)?.text =
            getString(R.string.location_set, "%.4f".format(lat), "%.4f".format(lng))
    }

    private fun showError(tvError: TextView, message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "ReportIncident"
        private const val SYNC_WORK_NAME = "report_sync"
    }
}