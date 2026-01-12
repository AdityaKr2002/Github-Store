package zed.rainxch.githubstore.feature.favourites.presentation.mappers

import zed.rainxch.githubstore.core.data.local.db.entities.FavoriteRepo
import zed.rainxch.githubstore.core.presentation.utils.formatAddedAt
import zed.rainxch.githubstore.feature.favourites.presentation.model.FavouriteRepository

suspend fun FavoriteRepo.toFavouriteRepositoryUi(): FavouriteRepository {
    return FavouriteRepository(
        repoId = repoId,
        repoName = repoName,
        repoOwner = repoOwner,
        repoOwnerAvatarUrl = repoOwnerAvatarUrl,
        repoDescription = repoDescription,
        primaryLanguage = primaryLanguage,
        repoUrl = repoUrl,
        latestRelease = latestVersion,
        latestReleaseUrl = latestReleaseUrl,
        addedAtFormatter = formatAddedAt(addedAt)
    )
}