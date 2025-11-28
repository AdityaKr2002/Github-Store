package zed.rainxch.githubstore.feature.search.data.repository.dto

import kotlinx.serialization.Serializable

@Serializable
data class GithubReleaseNetworkModel(
    val draft: Boolean? = null,
    val prerelease: Boolean? = null,
    val assets: List<AssetNetworkModel>
)