package zed.rainxch.core.data.services

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import co.touchlab.kermit.Logger
import java.util.concurrent.TimeUnit

/**
 * Manages scheduling and cancellation of periodic update checks using WorkManager.
 *
 * Default schedule: every 6 hours with network connectivity constraint.
 * Uses exponential backoff for retries with a 30-minute initial delay.
 */
object UpdateScheduler {

    private const val DEFAULT_INTERVAL_HOURS = 6L

    /**
     * Schedules periodic update checks. Safe to call multiple times —
     * existing work is kept unless [replace] is true.
     */
    fun schedule(
        context: Context,
        intervalHours: Long = DEFAULT_INTERVAL_HOURS,
        replace: Boolean = false
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
            intervalHours, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.MINUTES
            )
            .build()

        val policy = if (replace) {
            ExistingPeriodicWorkPolicy.UPDATE
        } else {
            ExistingPeriodicWorkPolicy.KEEP
        }

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                UpdateCheckWorker.WORK_NAME,
                policy,
                request
            )

        Logger.i { "UpdateScheduler: Scheduled periodic update check every ${intervalHours}h (policy=$policy)" }
    }

    /**
     * Cancels the scheduled periodic update checks.
     */
    fun cancel(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(UpdateCheckWorker.WORK_NAME)
        Logger.i { "UpdateScheduler: Cancelled periodic update checks" }
    }
}
