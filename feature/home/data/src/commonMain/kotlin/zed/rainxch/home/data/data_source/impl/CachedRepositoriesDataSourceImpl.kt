package zed.rainxch.home.data.data_source.impl

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import zed.rainxch.core.domain.logging.GitHubStoreLogger
import zed.rainxch.core.domain.model.Platform
import zed.rainxch.home.data.data_source.CachedRepositoriesDataSource
import zed.rainxch.home.data.dto.CachedRepoResponse
import zed.rainxch.home.domain.model.HomeCategory

class CachedRepositoriesDataSourceImpl(
    private val platform: Platform,
    private val logger: GitHubStoreLogger
) : CachedRepositoriesDataSource {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 15_000
        }

        install(HttpRequestRetry) {
            maxRetries = 2
            retryOnServerErrors(maxRetries = 2)
            exponentialDelay()
        }

        expectSuccess = false
    }

    override suspend fun getCachedTrendingRepos(): CachedRepoResponse? {
        return fetchCachedReposForCategory(HomeCategory.TRENDING)
    }

    override suspend fun getCachedHotReleaseRepos(): CachedRepoResponse? {
        return fetchCachedReposForCategory(HomeCategory.HOT_RELEASE)
    }

    override suspend fun getCachedMostPopularRepos(): CachedRepoResponse? {
        return fetchCachedReposForCategory(HomeCategory.MOST_POPULAR)
    }

    private suspend fun fetchCachedReposForCategory(
        category: HomeCategory
    ): CachedRepoResponse? {
        return withContext(Dispatchers.IO) {
            val platformName = when (platform) {
                Platform.ANDROID -> "android"
                Platform.WINDOWS -> "windows"
                Platform.MACOS -> "macos"
                Platform.LINUX -> "linux"
            }

            val path = when (category) {
                HomeCategory.TRENDING -> "cached-data/trending/$platformName.json"
                HomeCategory.HOT_RELEASE -> "cached-data/new-releases/$platformName.json"
                HomeCategory.MOST_POPULAR -> "cached-data/most-popular/$platformName.json"
            }

            val mirrorUrls = listOf(
                "https://raw.githubusercontent.com/OpenHub-Store/api/main/$path",
                "https://cdn.jsdelivr.net/gh/OpenHub-Store/api@main/$path",
                "https://cdn.statically.io/gh/OpenHub-Store/api/main/$path"
            )

            for (url in mirrorUrls) {
                try {
                    logger.debug("Fetching from: $url")
                    val response: HttpResponse = httpClient.get(url)

                    if (response.status.isSuccess()) {
                        val responseText = response.bodyAsText()
                        return@withContext json.decodeFromString(responseText)
                    }
                } catch (e: Exception) {
                    logger.error("Error with $url: ${e.message}")
                }
            }

            logger.error("🚫 All mirrors failed for $category")
            null
        }
    }


    private companion object {
        private const val BASE_REPO_URL = "https://raw.githubusercontent.com/OpenHub-Store/api/refs/heads"
        private const val TRENDING_FULL_URL = "$BASE_REPO_URL/main/cached-data/trending"
        private const val HOT_RELEASE_FULL_URL = "$BASE_REPO_URL/main/cached-data/new-releases"
        private const val MOST_POPULAR_FULL_URL = "$BASE_REPO_URL/main/cached-data/most-popular"


    }
}