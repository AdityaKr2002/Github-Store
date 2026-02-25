package zed.rainxch.githubstore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import zed.rainxch.core.data.network.ProxyManager
import zed.rainxch.core.domain.model.ProxyConfig
import zed.rainxch.core.domain.repository.AuthenticationState
import zed.rainxch.core.domain.repository.InstalledAppsRepository
import zed.rainxch.core.domain.repository.ProxyRepository
import zed.rainxch.core.domain.repository.RateLimitRepository
import zed.rainxch.core.domain.repository.ThemesRepository
import zed.rainxch.core.domain.use_cases.SyncInstalledAppsUseCase

class MainViewModel(
    private val themesRepository: ThemesRepository,
    private val installedAppsRepository: InstalledAppsRepository,
    private val authenticationState: AuthenticationState,
    private val rateLimitRepository: RateLimitRepository,
    private val syncUseCase: SyncInstalledAppsUseCase,
    private val proxyRepository: ProxyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            authenticationState
                .isUserLoggedIn()
                .collect { isLoggedIn ->
                    _state.update { it.copy(isLoggedIn = isLoggedIn) }

                    if (isLoggedIn) {
                        rateLimitRepository.clear()
                    }
                }
        }

        viewModelScope.launch {
            themesRepository
                .getThemeColor()
                .collect { theme ->
                    _state.update {
                        it.copy(currentColorTheme = theme)
                    }
                }
        }
        viewModelScope.launch {
            themesRepository
                .getAmoledTheme()
                .collect { isAmoled ->
                    _state.update {
                        it.copy(isAmoledTheme = isAmoled)
                    }
                }
        }
        viewModelScope.launch {
            themesRepository
                .getIsDarkTheme()
                .collect { isDarkTheme ->
                    _state.update {
                        it.copy(isDarkTheme = isDarkTheme)
                    }
                }
        }

        viewModelScope.launch {
            themesRepository
                .getFontTheme()
                .collect { fontTheme ->
                    _state.update {
                        it.copy(currentFontTheme = fontTheme)
                    }
                }
        }

        viewModelScope.launch {
            rateLimitRepository.rateLimitState.collect { rateLimitInfo ->
                _state.update { currentState ->
                    currentState.copy(rateLimitInfo = rateLimitInfo)
                }
            }
        }

        viewModelScope.launch {
            rateLimitRepository.rateLimitExhaustedEvent.collect { info ->
                _state.update { it.copy(showRateLimitDialog = true, rateLimitInfo = info) }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            syncUseCase().onSuccess {
                installedAppsRepository.checkAllForUpdates()
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val savedConfig = proxyRepository.getProxyConfig().first()
            when (savedConfig) {
                is ProxyConfig.None -> ProxyManager.setNoProxy()
                is ProxyConfig.System -> ProxyManager.setSystemProxy()
                is ProxyConfig.Http -> ProxyManager.setHttpProxy(
                    host = savedConfig.host,
                    port = savedConfig.port,
                    username = savedConfig.username,
                    password = savedConfig.password
                )
                is ProxyConfig.Socks -> ProxyManager.setSocksProxy(
                    host = savedConfig.host,
                    port = savedConfig.port,
                    username = savedConfig.username,
                    password = savedConfig.password
                )
            }
        }
    }

    fun onAction(action: MainAction) {
        when (action) {
            MainAction.DismissRateLimitDialog -> {
                _state.update { it.copy(showRateLimitDialog = false) }
            }
        }
    }
}