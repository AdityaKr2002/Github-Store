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

object UpdateScheduler {

    private const val DEFAULT_INTERVAL_HOURS = 6L

    fun schedule(
        context: Context,
        intervalHours: Long = DEFAULT_INTERVAL_HOURS,
        replace: Boolean = false
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
            repeatInterval = intervalHours,
            repeatIntervalTimeUnit = TimeUnit.HOURS
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
                uniqueWorkName = UpdateCheckWorker.WORK_NAME,
                existingPeriodicWorkPolicy = policy,
                request = request
            )

        Logger.i { "UpdateScheduler: Scheduled periodic update check every ${intervalHours}h (policy=$policy)" }
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(UpdateCheckWorker.WORK_NAME)
        Logger.i { "UpdateScheduler: Cancelled periodic update checks" }
    }
}
