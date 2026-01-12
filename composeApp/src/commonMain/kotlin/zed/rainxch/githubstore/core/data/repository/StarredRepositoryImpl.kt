@file:OptIn(ExperimentalTime::class)

package zed.rainxch.githubstore.core.data.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import zed.rainxch.githubstore.core.data.local.db.dao.InstalledAppDao
import zed.rainxch.githubstore.core.data.local.db.dao.StarredRepoDao
import zed.rainxch.githubstore.core.data.local.db.entities.StarredRepo
import zed.rainxch.githubstore.core.data.model.GitHubStarredResponse
import zed.rainxch.githubstore.core.domain.repository.StarredRepository
import zed.rainxch.githubstore.feature.details.domain.repository.DetailsRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class StarredRepositoryImpl(
    private val dao: StarredRepoDao,
    private val installedAppsDao: InstalledAppDao,
    private val detailsRepository: DetailsRepository,
    private val httpClient: HttpClient
) : StarredRepository {

    companion object {
        private const val SYNC_THRESHOLD_MS = 6 * 60 * 60 * 1000L // 6 hours
    }

    override fun getAllStarred(): Flow<List<StarredRepo>> = dao.getAllStarred()

    override fun isStarred(repoId: Long): Flow<Boolean> = dao.isStarred(repoId)

    override suspend fun isStarredSync(repoId: Long): Boolean = dao.isStarredSync(repoId)

    override suspend fun getLastSyncTime(): Long? = dao.getLastSyncTime()

    override suspend fun needsSync(): Boolean {
        val lastSync = getLastSyncTime() ?: return true
        val now = Clock.System.now().toEpochMilliseconds()
        return (now - lastSync) > SYNC_THRESHOLD_MS
    }

    override suspend fun syncStarredRepos(forceRefresh: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                if (!forceRefresh && !needsSync()) {
                    return@withContext Result.success(Unit)
                }

                val starredRepos = mutableListOf<StarredRepo>()
                var page = 1
                val perPage = 100

                while (true) {
                    val response = httpClient.get("/user/starred") {
                        parameter("per_page", perPage)
                        parameter("page", page)
                    }

                    if (!response.status.isSuccess()) {
                        if (response.status.value == 401) {
                            return@withContext Result.failure(
                                Exception("Authentication required. Please sign in with GitHub.")
                            )
                        }
                        return@withContext Result.failure(
                            Exception("Failed to fetch starred repos: ${response.status.description}")
                        )
                    }

                    val repos: List<GitHubStarredResponse> = response.body()

                    if (repos.isEmpty()) break

                    val now = Clock.System.now().toEpochMilliseconds()

                    repos.forEach { repo ->
                        // Check if repo has valid release assets
                        val hasValidAssets = checkForValidAssets(repo.owner.login, repo.name)

                        if (hasValidAssets) {
                            val installedApp = installedAppsDao.getAppByRepoId(repo.id)

                            starredRepos.add(
                                StarredRepo(
                                    repoId = repo.id,
                                    repoName = repo.name,
                                    repoOwner = repo.owner.login,
                                    repoOwnerAvatarUrl = repo.owner.avatarUrl,
                                    repoDescription = repo.description,
                                    primaryLanguage = repo.language,
                                    repoUrl = repo.htmlUrl,
                                    stargazersCount = repo.stargazersCount,
                                    forksCount = repo.forksCount,
                                    openIssuesCount = repo.openIssuesCount,
                                    isInstalled = installedApp != null,
                                    installedPackageName = installedApp?.packageName,
                                    latestVersion = null,
                                    latestReleaseUrl = null,
                                    starredAt = repo.starredAt?.let {
                                        Instant.parse(it).toEpochMilliseconds()
                                    },
                                    addedAt = now,
                                    lastSyncedAt = now
                                )
                            )
                        }
                    }

                    if (repos.size < perPage) break
                    page++
                }

                // Replace all starred repos
                dao.clearAll()
                dao.insertAllStarred(starredRepos)

                Result.success(Unit)
            } catch (e: Exception) {
                Logger.e(e) { "Failed to sync starred repos" }
                Result.failure(e)
            }
        }

    private suspend fun checkForValidAssets(owner: String, repo: String): Boolean {
        return try {
            val release = detailsRepository.getLatestPublishedRelease(
                owner = owner,
                repo = repo,
                defaultBranch = ""
            )

            release?.assets?.any { asset ->
                val name = asset.name.lowercase()
                name.endsWith(".apk") ||
                        name.endsWith(".exe") ||
                        name.endsWith(".msi") ||
                        name.endsWith(".dmg") ||
                        name.endsWith(".pkg") ||
                        name.endsWith(".deb") ||
                        name.endsWith(".rpm") ||
                        name.endsWith(".appimage")
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun addStarred(repo: StarredRepo) {
        val installedApp = installedAppsDao.getAppByRepoId(repo.repoId)
        dao.insertStarred(
            repo.copy(
                isInstalled = installedApp != null,
                installedPackageName = installedApp?.packageName
            )
        )
    }

    override suspend fun removeStarred(repoId: Long) {
        dao.deleteStarredById(repoId)
    }

    override suspend fun updateStarredInstallStatus(
        repoId: Long,
        installed: Boolean,
        packageName: String?
    ) {
        dao.updateInstallStatus(repoId, installed, packageName)
    }

    override suspend fun syncStarredVersions() {
        val starred = dao.getAllStarred().first()
        starred.forEach { starredRepo ->
            try {
                val latestRelease = detailsRepository.getLatestPublishedRelease(
                    owner = starredRepo.repoOwner,
                    repo = starredRepo.repoName,
                    defaultBranch = ""
                )

                dao.updateLatestVersion(
                    repoId = starredRepo.repoId,
                    version = latestRelease?.tagName,
                    releaseUrl = latestRelease?.htmlUrl,
                    timestamp = Clock.System.now().toEpochMilliseconds()
                )
            } catch (e: Exception) {
                Logger.e(e) { "Failed to sync version for ${starredRepo.repoName}" }
            }
        }
    }
}