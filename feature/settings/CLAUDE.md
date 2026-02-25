# CLAUDE.md - Settings Feature

## Purpose

App settings and preferences. Organized into three sections: **Account** (login status, logout), **Appearance** (theme colors, dark mode, AMOLED, font), and **About** (version info, links).

## Module Structure

```
feature/settings/
├── domain/
│   └── repository/SettingsRepository.kt  # Login state, logout, version
├── data/
│   ├── di/SharedModule.kt            # Koin: settingsModule
│   └── repository/SettingsRepositoryImpl.kt
└── presentation/
    ├── SettingsViewModel.kt           # Settings state, theme changes, logout
    ├── SettingsState.kt               # isLoggedIn, theme, darkMode, amoled, font, version
    ├── SettingsAction.kt              # Theme change, toggle dark/amoled, logout, etc.
    ├── SettingsEvent.kt               # One-off events
    ├── SettingsRoot.kt                # Main composable (scrollable settings list)
    └── components/
        ├── LogoutDialog.kt            # Confirmation dialog for logout
        └── sections/
            ├── Account.kt             # Login status + logout button
            ├── Appearance.kt          # Theme picker, dark mode, AMOLED, font
            └── About.kt               # Version, links, credits
```

## Key Interfaces

```kotlin
interface SettingsRepository {
    val isUserLoggedIn: Flow<Boolean>
    suspend fun logout()
    fun getVersionName(): String
}
```

## Navigation

Route: `GithubStoreGraph.SettingsScreen` (data object, no params)

## Implementation Notes

- Theme settings persist via `ThemesRepository` from core/domain (DataStore-backed)
- `SettingsViewModel` also depends on `ThemesRepository` for reading/writing appearance preferences
- Logout clears the token via `TokenStore` and resets auth state
- App theme (colors, dark mode, AMOLED, font) is applied app-wide through `MainViewModel` which observes `ThemesRepository`
- Version name is read from build config at runtime
