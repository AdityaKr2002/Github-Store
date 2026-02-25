# CLAUDE.md - Details Feature

## Purpose

Repository detail screen. Displays full info for a GitHub repository including owner profile, stats, releases with download links, readme rendering, and installation/update flow. This is the most complex feature module (~27 presentation files).

## Module Structure

```
feature/details/
├── domain/
│   ├── model/
│   │   ├── ReleaseCategory.kt        # Release filtering categories
│   │   └── RepoStats.kt              # Stars, forks, open issues
│   └── repository/DetailsRepository.kt  # Repo, releases, readme, stats, user profile
├── data/
│   ├── di/SharedModule.kt            # Koin: detailsModule
│   ├── repository/DetailsRepositoryImpl.kt  # API calls + readme localization
│   ├── readme/ReadmeLocalizationHelper.kt   # Find readme in user's language
│   ├── dto/                           # Network DTOs
│   └── mappers/                       # DTO → domain model mappers
└── presentation/
    ├── DetailsViewModel.kt            # State management for detail screen
    ├── DetailsState.kt                # Repo, releases, readme, download progress, etc.
    ├── DetailsAction.kt               # Load, download, install, favourite, star, etc.
    ├── DetailsEvent.kt                # Navigation, toast events
    ├── DetailsRoot.kt                 # Main composable
    ├── model/
    │   ├── DownloadStage.kt           # Download progress tracking
    │   ├── InstallLogItem.kt          # Installation log entries
    │   └── LogResult.kt               # Log result types
    ├── components/
    │   ├── AppHeader.kt               # App icon, name, developer
    │   ├── SmartInstallButton.kt      # Context-aware install/update/open button
    │   ├── StatItem.kt                # Individual stat display
    │   ├── VersionPicker.kt           # Release version selector
    │   └── sections/
    │       ├── About.kt               # Description & topics
    │       ├── Header.kt              # Top header section
    │       ├── Logs.kt                # Installation/download logs
    │       ├── Owner.kt               # Repository owner info
    │       ├── Stats.kt               # Stars, forks, issues
    │       └── WhatsNew.kt            # Release changelog
    ├── states/ErrorState.kt           # Error display composable
    └── utils/
        ├── LocalTopbarLiquidState.kt
        ├── LogResultAsText.kt         # Log result formatting
        ├── MarkdownImageTransformer.kt  # Transform relative image URLs
        ├── MarkdownUtils.kt           # Markdown preprocessing
        ├── SystemArchitecture.kt      # Platform architecture detection
        └── isLiquidFrostAvailable.kt
```

## Key Interfaces

```kotlin
interface DetailsRepository {
    suspend fun getRepositoryById(id: Long): GithubRepoSummary
    suspend fun getRepositoryByOwnerAndName(owner: String, name: String): GithubRepoSummary
    suspend fun getLatestPublishedRelease(owner: String, repo: String, defaultBranch: String): GithubRelease?
    suspend fun getAllReleases(owner: String, repo: String, defaultBranch: String): List<GithubRelease>
    suspend fun getReadme(owner: String, repo: String, defaultBranch: String): Triple<ReadmeContent, LanguageCode?, ReadmePath>?
    suspend fun getRepoStats(owner: String, repo: String): RepoStats
    suspend fun getUserProfile(username: String): GithubUserProfile
}
```

## Navigation

Route: `GithubStoreGraph.DetailsScreen(repositoryId: Long, owner: String, repo: String)`

Can be reached via repo ID or owner+name (for deep links). Falls back to owner+name lookup if `repositoryId == -1`.

## Implementation Notes

- Readme supports localization: `ReadmeLocalizationHelper` tries to find readme in user's language first
- Markdown rendering uses `multiplatform-markdown-renderer` with custom `MarkdownImageTransformer` for relative URLs
- Download flow tracks stages via `DownloadStage` (idle → downloading → installing → done)
- `SmartInstallButton` changes behavior based on installed/update-available/not-installed state
- Version picker allows selecting specific releases for download
- Integrates with `FavouritesRepository`, `StarredRepository`, `InstalledAppsRepository` from core
- Uses `Downloader` and `Installer` interfaces from core/domain for platform-specific download/install
