package com.project.umlindi.ui.home

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.umlindi.R
import java.util.concurrent.TimeUnit

class ReportsAdapter(private val reports: MutableList<Report> = mutableListOf()) :
    RecyclerView.Adapter<ReportsAdapter.ReportViewHolder>() {

    fun submitList(newReports: List<Report>) {
        reports.clear()
        reports.addAll(newReports)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    override fun getItemCount() = reports.size

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val severityDot: View = itemView.findViewById(R.id.severityDot)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvTimeAgo: TextView = itemView.findViewById(R.id.tvTimeAgo)

        fun bind(report: Report) {
            tvCategory.text = formatCategory(report.category)
            tvDescription.text = report.description
            tvTimeAgo.text = formatTimeAgo(report.createdAt?.toDate()?.time)

            val color = when (report.category) {
                "break_in" -> Color.parseColor("#D32F2F")       // high severity — red
                "suspicious_activity" -> Color.parseColor("#F57C00") // medium — orange
                else -> Color.parseColor("#FBC02D")              // low — yellow
            }
            (severityDot.background as? GradientDrawable)?.setColor(color)
        }

        private fun formatCategory(raw: String): String = when (raw) {
            "break_in" -> "Break-in in progress"
            "suspicious_activity" -> "Suspicious activity"
            "load_shedding_risk" -> "Load-shedding risk"
            else -> "Other incident"
        }

        private fun formatTimeAgo(timestampMillis: Long?): String {
            if (timestampMillis == null) return ""
            val diff = System.currentTimeMillis() - timestampMillis
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            return when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "$minutes min ago"
                else -> "${TimeUnit.MINUTES.toHours(minutes)} hr ago"
            }
        }
    }
}