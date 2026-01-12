package zed.rainxch.githubstore.core.domain.repository

import kotlinx.coroutines.flow.Flow
import zed.rainxch.githubstore.core.data.local.db.entities.StarredRepo

interface StarredRepository {
    fun getAllStarred(): Flow<List<StarredRepo>>
    fun isStarred(repoId: Long): Flow<Boolean>
    suspend fun isStarredSync(repoId: Long): Boolean

    suspend fun syncStarredRepos(forceRefresh: Boolean = false): Result<Unit>

    suspend fun addStarred(repo: StarredRepo)
    suspend fun removeStarred(repoId: Long)
    
    suspend fun updateStarredInstallStatus(repoId: Long, installed: Boolean, packageName: String?)
    suspend fun syncStarredVersions()
    
    suspend fun getLastSyncTime(): Long?
    suspend fun needsSync(): Boolean
}