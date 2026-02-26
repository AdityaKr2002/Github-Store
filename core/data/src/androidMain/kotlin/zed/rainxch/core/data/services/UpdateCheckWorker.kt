package zed.rainxch.core.data.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import zed.rainxch.core.domain.repository.InstalledAppsRepository
import zed.rainxch.core.domain.use_cases.SyncInstalledAppsUseCase

/**
 * Periodic background worker that checks all tracked installed apps for available updates.
 *
 * Runs via WorkManager on a configurable schedule (default: every 6 hours).
 * First syncs app state with the system package manager, then checks each
 * tracked app's GitHub repository for new releases.
 */
class UpdateCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val installedAppsRepository: InstalledAppsRepository by inject()
    private val syncInstalledAppsUseCase: SyncInstalledAppsUseCase by inject()

    override suspend fun doWork(): Result {
        return try {
            Logger.i { "UpdateCheckWorker: Starting periodic update check" }

            // First sync installed apps state with system
            val syncResult = syncInstalledAppsUseCase()
            if (syncResult.isFailure) {
                Logger.w { "UpdateCheckWorker: Sync had issues: ${syncResult.exceptionOrNull()?.message}" }
            }

            // Check all tracked apps for updates
            installedAppsRepository.checkAllForUpdates()

            Logger.i { "UpdateCheckWorker: Periodic update check completed successfully" }
            Result.success()
        } catch (e: Exception) {
            Logger.e { "UpdateCheckWorker: Update check failed: ${e.message}" }
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "github_store_update_check"
    }
}
