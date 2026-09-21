package com.project.umlindi.ui.home

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.project.umlindi.R

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val firestore = FirebaseFirestore.getInstance()
    private val adapter = ReportsAdapter()
    private var listenerRegistration: ListenerRegistration? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        listenerRegistration = firestore.collection("reports")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val reports = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Report::class.java)?.copy(reportId = doc.id)
                }
                adapter.submitList(reports)
                tvEmpty.visibility = if (reports.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
    }
}