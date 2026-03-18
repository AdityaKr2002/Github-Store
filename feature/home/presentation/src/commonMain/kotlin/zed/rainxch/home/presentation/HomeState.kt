package zed.rainxch.home.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import zed.rainxch.core.domain.model.InstalledApp
import zed.rainxch.core.presentation.model.DiscoveryRepositoryUi
import zed.rainxch.home.domain.model.HomeCategory

data class HomeState(
    val repos: ImmutableList<DiscoveryRepositoryUi> = persistentListOf(),
    val installedApps: ImmutableList<InstalledApp> = persistentListOf(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val hasMorePages: Boolean = true,
    val currentCategory: HomeCategory = HomeCategory.TRENDING,
    val isAppsSectionVisible: Boolean = false,
    val isUpdateAvailable: Boolean = false,
)
