package com.project.umlindi.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.project.umlindi.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class ReportSyncWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ReportSyncWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val dao = AppDatabase.getInstance(applicationContext).pendingReportDao()
        val pending = dao.getAll()

        if (pending.isEmpty()) {
            Log.d(TAG, "No pending reports to sync")
            return@withContext Result.success()
        }

        Log.d(TAG, "Syncing ${pending.size} pending report(s)")
        val firestore = FirebaseFirestore.getInstance()
        var allSucceeded = true

        for (item in pending) {
            try {
                val reportMap = hashMapOf(
                    "reporterId" to item.reporterId,
                    "category" to item.category,
                    "description" to item.description,
                    "location" to GeoPoint(item.latitude, item.longitude),
                    "status" to "pending",
                    "createdAt" to Timestamp(Date(item.createdAtMillis)),
                    "updatedAt" to Timestamp.now()
                )
                Tasks.await(firestore.collection("reports").add(reportMap))
                dao.delete(item)
                Log.d(TAG, "Synced and removed local report ${item.localId}")
            } catch (e: Exception) {
                allSucceeded = false
                Log.e(TAG, "Failed to sync local report ${item.localId}", e)
            }
        }

        if (allSucceeded) Result.success() else Result.retry()
    }
}