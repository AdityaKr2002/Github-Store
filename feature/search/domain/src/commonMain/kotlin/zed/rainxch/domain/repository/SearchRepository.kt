package zed.rainxch.domain.repository

import kotlinx.coroutines.flow.Flow
import zed.rainxch.core.domain.model.PaginatedDiscoveryRepositories
import zed.rainxch.domain.model.ProgrammingLanguage
import zed.rainxch.domain.model.SearchPlatform
import zed.rainxch.domain.model.SortBy

interface SearchRepository {
    fun searchRepositories(
        query: String,
        searchPlatform: SearchPlatform,
        language: ProgrammingLanguage,
        sortBy: SortBy,
        page: Int
    ): Flow<PaginatedDiscoveryRepositories>
}