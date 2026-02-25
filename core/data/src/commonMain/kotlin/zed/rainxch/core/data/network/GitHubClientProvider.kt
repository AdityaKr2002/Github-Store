package zed.rainxch.core.data.network

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import zed.rainxch.core.data.data_source.TokenStore
import zed.rainxch.core.domain.model.ProxyConfig
import zed.rainxch.core.domain.repository.RateLimitRepository

class GitHubClientProvider(
    private val tokenStore: TokenStore,
    private val rateLimitRepository: RateLimitRepository,
    proxyConfigFlow: StateFlow<ProxyConfig>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var currentClient: HttpClient? = null
    private val mutex = Mutex()

    private val _client: StateFlow<HttpClient> = proxyConfigFlow
        .map { proxyConfig ->
            mutex.withLock {
                currentClient?.close()
                val newClient = createGitHubHttpClient(
                    tokenStore = tokenStore,
                    rateLimitRepository = rateLimitRepository,
                    proxyConfig = proxyConfig
                )
                currentClient = newClient
                newClient
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = createGitHubHttpClient(
                tokenStore = tokenStore,
                rateLimitRepository = rateLimitRepository,
                proxyConfig = proxyConfigFlow.value
            ).also { currentClient = it }
        )

    /** Get the current HttpClient (always up to date with proxy settings) */
    val client: HttpClient get() = _client.value

    fun close() {
        currentClient?.close()
        scope.cancel()
    }
}