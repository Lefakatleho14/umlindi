package com.project.umlindi.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.project.umlindi.R
import com.project.umlindi.util.AlertRules
import com.project.umlindi.util.GeoUtils
import com.project.umlindi.util.NotificationHelper
import com.project.umlindi.util.ReportFormatter
import androidx.core.os.bundleOf
import com.project.umlindi.ui.detail.AlertDetailFragment

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val firestore = FirebaseFirestore.getInstance()
    private val adapter = ReportsAdapter(onItemClick = { report ->
        findNavController().navigate(
            R.id.action_home_to_alertDetail,
            bundleOf(AlertDetailFragment.ARG_REPORT_ID to report.reportId)
        )
    })
    private var listenerRegistration: ListenerRegistration? = null
    private var isFirstLoad = true

    private var homeLocation: GeoPoint? = null
    private var alertRadiusKm = DEFAULT_RADIUS_KM

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.d(TAG, "Notification permission granted=$granted")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val rvReports = view.findViewById<RecyclerView>(R.id.rvReports)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        rvReports.layoutManager = LinearLayoutManager(requireContext())
        rvReports.adapter = adapter

        view.findViewById<View>(R.id.fabReport).setOnClickListener {
            findNavController().navigate(R.id.action_home_to_reportIncident)
        }

        view.findViewById<View>(R.id.btnSettings).setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId != null) {
            firestore.collection("users").document(currentUserId).get()
                .addOnSuccessListener { doc ->
                    homeLocation = doc.getGeoPoint("homeLocation")
                    alertRadiusKm = doc.getDouble("alertRadiusKm") ?: DEFAULT_RADIUS_KM
                    Log.d(TAG, "Loaded alert settings: radius=$alertRadiusKm, hasHome=${homeLocation != null}")
                }
                .addOnFailureListener { e -> Log.e(TAG, "Could not load alert settings", e) }
        }

        listenerRegistration = firestore.collection("reports")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e(TAG, "Reports listener error", error)
                    return@addSnapshotListener
                }

                val reports = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Report::class.java)?.copy(reportId = doc.id)
                }
                adapter.submitList(reports)
                tvEmpty.visibility = if (reports.isEmpty()) View.VISIBLE else View.GONE

                if (!isFirstLoad) {
                    for (change in snapshot.documentChanges) {
                        if (change.type != DocumentChange.Type.ADDED) continue
                        val report = change.document.toObject(Report::class.java)
                        if (report.reporterId == currentUserId) continue

                        val home = homeLocation
                        val reportLocation = report.location
                        val distanceKm = if (home != null && reportLocation != null) {
                            GeoUtils.distanceKm(
                                home.latitude, home.longitude,
                                reportLocation.latitude, reportLocation.longitude
                            )
                        } else {
                            null
                        }

                        if (!AlertRules.shouldNotify(report.category, distanceKm, alertRadiusKm)) {
                            Log.d(TAG, "Report outside alert radius (distance=$distanceKm km), no notification")
                            continue
                        }

                        Log.d(TAG, "Nearby report from another user, notifying")
                        NotificationHelper.showNewReportNotification(
                            requireContext(),
                            getString(ReportFormatter.categoryLabelRes(report.category)),
                            report.description,
                            change.document.id.hashCode()
                        )
                    }
                }
                isFirstLoad = false
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
    }

    companion object {
        private const val TAG = "HomeFragment"
        private const val DEFAULT_RADIUS_KM = 5.0
    }
}