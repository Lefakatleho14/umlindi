package com.project.umlindi.ui.home

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.umlindi.R
import com.project.umlindi.util.ReportFormatter
import com.project.umlindi.util.TimeAgo

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
            val context = itemView.context

            tvCategory.text = context.getString(ReportFormatter.categoryLabelRes(report.category))
            tvDescription.text = report.description
            tvTimeAgo.text = when (val ago = ReportFormatter.timeAgo(report.createdAt?.toDate()?.time)) {
                TimeAgo.Unknown -> ""
                TimeAgo.JustNow -> context.getString(R.string.time_just_now)
                is TimeAgo.Minutes -> context.getString(R.string.time_minutes_ago, ago.count.toInt())
                is TimeAgo.Hours -> context.getString(R.string.time_hours_ago, ago.count.toInt())
            }

            val color = Color.parseColor(ReportFormatter.severityColorHex(report.category))
            (severityDot.background as? GradientDrawable)?.setColor(color)
        }
    }
}