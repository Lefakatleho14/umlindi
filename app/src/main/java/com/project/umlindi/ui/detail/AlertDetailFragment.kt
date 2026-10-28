package com.project.umlindi.ui.detail

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.project.umlindi.R
import com.project.umlindi.ui.home.Report
import com.project.umlindi.util.GeoUtils
import com.project.umlindi.util.ReportFormatter
import com.project.umlindi.util.TrustRules
import com.project.umlindi.util.toText
import kotlin.math.roundToInt

class AlertDetailFragment : Fragment(R.layout.fragment_alert_detail) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var reportListener: ListenerRegistration? = null
    private var votesListener: ListenerRegistration? = null
    private var reporterListener: ListenerRegistration? = null

    private var report: Report? = null
    private var myVote: String? = null
    private var viewerHome: GeoPoint? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reportId = arguments?.getString(ARG_REPORT_ID)
        val uid = auth.currentUser?.uid
        if (reportId == null || uid == null) {
            findNavController().popBackStack()
            return
        }

        view.findViewById<View>(R.id.btnBack).setOnClickListener { findNavController().popBackStack() }
        view.findViewById<View>(R.id.btnConfirm).setOnClickListener { castVote(reportId, uid, TrustRules.CONFIRM) }
        view.findViewById<View>(R.id.btnDispute).setOnClickListener { castVote(reportId, uid, TrustRules.DISPUTE) }

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                viewerHome = doc.getGeoPoint("homeLocation")
                bindReport()
            }
            .addOnFailureListener { e -> Log.e(TAG, "Could not load viewer profile", e) }

        val reportRef = firestore.collection("reports").document(reportId)

        reportListener = reportRef.addSnapshotListener { doc, error ->
            if (error != null) {
                Log.e(TAG, "Report listener error", error)
                return@addSnapshotListener
            }
            if (doc == null || !doc.exists()) {
                showError(getString(R.string.report_not_found))
                return@addSnapshotListener
            }
            val loaded = doc.toObject(Report::class.java)?.copy(reportId = doc.id)
                ?: return@addSnapshotListener
            val reporterChanged = report?.reporterId != loaded.reporterId
            report = loaded
            bindReport()
            if (reporterChanged) listenToReporter(loaded.reporterId)
        }

        votesListener = reportRef.collection("votes").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                Log.e(TAG, "Votes listener error", error)
                return@addSnapshotListener
            }
            var confirms = 0
            var disputes = 0
            for (vote in snapshot.documents) {
                when (vote.getString("voteType")) {
                    TrustRules.CONFIRM -> confirms++
                    TrustRules.DISPUTE -> disputes++
                }
            }
            myVote = snapshot.documents.firstOrNull { it.id == uid }?.getString("voteType")
            bindVotes(confirms, disputes)
        }
    }

    private fun bindReport() {
        val root = view ?: return
        val current = report ?: return

        root.findViewById<TextView>(R.id.tvCategory).text =
            getString(ReportFormatter.categoryLabelRes(current.category))
        root.findViewById<TextView>(R.id.tvDescription).text = current.description

        val badgeColor = Color.parseColor(ReportFormatter.severityBadgeColorHex(current.category))
        root.findViewById<TextView>(R.id.tvSeverity).apply {
            text = getString(ReportFormatter.severityLabelRes(current.category))
            setTextColor(badgeColor)
            (background.mutate() as? GradientDrawable)
                ?.setColor(ColorUtils.setAlphaComponent(badgeColor, 36))
        }

        val timeText = ReportFormatter.timeAgo(current.createdAt?.toDate()?.time).toText(root.context)
        root.findViewById<TextView>(R.id.tvMeta).text =
            listOf(distanceText(current.location), timeText)
                .filter { it.isNotEmpty() }
                .joinToString(" • ")

        val isOwnReport = current.reporterId == auth.currentUser?.uid
        root.findViewById<View>(R.id.voteButtons).visibility = if (isOwnReport) View.GONE else View.VISIBLE
        root.findViewById<View>(R.id.tvOwnReportNote).visibility = if (isOwnReport) View.VISIBLE else View.GONE
    }

    private fun bindVotes(confirms: Int, disputes: Int) {
        val root = view ?: return

        root.findViewById<TextView>(R.id.tvFeedbackCounts).text =
            getString(R.string.feedback_counts, confirms, disputes)

        val statusView = root.findViewById<TextView>(R.id.tvVoteStatus)
        when (myVote) {
            TrustRules.CONFIRM -> {
                statusView.text = getString(R.string.you_confirmed)
                statusView.visibility = View.VISIBLE
            }
            TrustRules.DISPUTE -> {
                statusView.text = getString(R.string.you_disputed)
                statusView.visibility = View.VISIBLE
            }
            else -> statusView.visibility = View.GONE
        }

        root.findViewById<View>(R.id.btnConfirm).alpha = if (myVote == TrustRules.DISPUTE) 0.55f else 1f
        root.findViewById<View>(R.id.btnDispute).alpha = if (myVote == TrustRules.CONFIRM) 0.55f else 1f
    }

    private fun listenToReporter(reporterId: String) {
        reporterListener?.remove()
        reporterListener = firestore.collection("users").document(reporterId)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    Log.e(TAG, "Could not load reporter", error)
                    return@addSnapshotListener
                }
                val root = view ?: return@addSnapshotListener
                val name = doc?.getString("displayName")?.takeIf { it.isNotBlank() }
                    ?: getString(R.string.unknown_reporter)
                val score = (doc?.getLong("trustScore") ?: 0L).toInt()

                root.findViewById<TextView>(R.id.tvInitials).text = name.take(1).uppercase()
                root.findViewById<TextView>(R.id.tvReporterName).text = name
                root.findViewById<TextView>(R.id.tvReporterTrust).text =
                    getString(R.string.trust_score_format, score)
            }
    }

    private fun castVote(reportId: String, uid: String, newVote: String) {
        val current = report ?: return
        if (myVote == newVote) return

        val voteRef = firestore.collection("reports").document(reportId)
            .collection("votes").document(uid)
        val reporterRef = firestore.collection("users").document(current.reporterId)

        setVoting(true)
        firestore.runTransaction { transaction ->
            val previousVote = transaction.get(voteRef).getString("voteType")
            val delta = TrustRules.delta(previousVote, newVote)
            if (delta != 0) {
                transaction.set(
                    voteRef,
                    mapOf("voteType" to newVote, "createdAt" to Timestamp.now())
                )
                transaction.update(reporterRef, "trustScore", FieldValue.increment(delta.toLong()))
            }
            null
        }
            .addOnSuccessListener {
                Log.d(TAG, "Vote recorded: $newVote")
                setVoting(false)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Vote failed", e)
                setVoting(false)
                showError(getString(R.string.error_vote_failed))
            }
    }

    private fun distanceText(location: GeoPoint?): String {
        val home = viewerHome ?: return ""
        val target = location ?: return ""
        val km = GeoUtils.distanceKm(home.latitude, home.longitude, target.latitude, target.longitude)
        return if (km < 1.0) {
            getString(R.string.distance_m_away, (km * 1000).roundToInt())
        } else {
            getString(R.string.distance_km_away, "%.1f".format(km))
        }
    }

    private fun setVoting(voting: Boolean) {
        val root = view ?: return
        root.findViewById<ProgressBar>(R.id.progressBar).visibility = if (voting) View.VISIBLE else View.GONE
        root.findViewById<View>(R.id.btnConfirm).isEnabled = !voting
        root.findViewById<View>(R.id.btnDispute).isEnabled = !voting
        if (voting) root.findViewById<TextView>(R.id.tvError).visibility = View.GONE
    }

    private fun showError(message: String) {
        view?.findViewById<TextView>(R.id.tvError)?.apply {
            text = message
            visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        reportListener?.remove()
        votesListener?.remove()
        reporterListener?.remove()
        super.onDestroyView()
    }

    companion object {
        const val ARG_REPORT_ID = "reportId"
        private const val TAG = "AlertDetailFragment"
    }
}