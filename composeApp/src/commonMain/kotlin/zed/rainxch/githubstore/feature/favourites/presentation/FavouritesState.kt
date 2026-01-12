package zed.rainxch.githubstore.feature.favourites.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import zed.rainxch.githubstore.feature.favourites.presentation.model.FavouriteRepository

data class FavouritesState(
    val favouriteRepositories: ImmutableList<FavouriteRepository> = persistentListOf(),
    val isLoading: Boolean = false,
)