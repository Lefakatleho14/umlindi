package com.project.umlindi.ui.home

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.project.umlindi.R
import com.project.umlindi.util.ReportFormatter
import com.project.umlindi.util.toText

class ReportsAdapter(
    private val onItemClick: (Report) -> Unit,
    private val reports: MutableList<Report> = mutableListOf()
) : RecyclerView.Adapter<ReportsAdapter.ReportViewHolder>() {

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
        holder.bind(reports[position], onItemClick)
    }

    override fun getItemCount() = reports.size

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val severityDot: View = itemView.findViewById(R.id.severityDot)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvTimeAgo: TextView = itemView.findViewById(R.id.tvTimeAgo)
        private val tvSeverity: TextView = itemView.findViewById(R.id.tvSeverity)

        fun bind(report: Report, onItemClick: (Report) -> Unit) {
            val context = itemView.context

            tvCategory.text = context.getString(ReportFormatter.categoryLabelRes(report.category))
            tvDescription.text = report.description
            tvTimeAgo.text = ReportFormatter.timeAgo(report.createdAt?.toDate()?.time).toText(context)

            val dotColor = Color.parseColor(ReportFormatter.severityColorHex(report.category))
            (severityDot.background.mutate() as? GradientDrawable)?.setColor(dotColor)

            val badgeColor = Color.parseColor(ReportFormatter.severityBadgeColorHex(report.category))
            tvSeverity.text = context.getString(ReportFormatter.severityLabelRes(report.category))
            tvSeverity.setTextColor(badgeColor)
            (tvSeverity.background.mutate() as? GradientDrawable)
                ?.setColor(ColorUtils.setAlphaComponent(badgeColor, 36))

            itemView.setOnClickListener { onItemClick(report) }
        }
    }
}