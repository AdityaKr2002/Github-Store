<div align="center">
</br>
<img src="https://github.com/XDdevv/Github-Store/blob/main/composeApp/src/commonMain/composeResources/drawable/app-icon.png" width="200" />

</div>

<h1 align="center">GithubStore</h1>

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://kotlinlang.org"><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF.svg"/></a>
  <a href="#"><img alt="Platforms" src="https://img.shields.io/badge/Platforms-Android%20%7C%20Desktop-brightgreen"/></a>
</p>

<p align="center">
GithubStore is a cross‑platform “play store” for GitHub releases.  
It discovers repositories that ship real installable binaries and lets you install the latest release in one click.
</p>

<!-- <p align="center">
  <img src="docs/preview-home.png" width="360"/>
  <img src="docs/preview-details.png" width="360"/>
</p> -->

---

## ✨ What is GithubStore?

GithubStore is a Kotlin Multiplatform app (Android + Desktop) that turns GitHub releases into a clean, app‑store style experience:

- Only shows repositories that actually provide installable assets (APK, EXE, DMG, etc.).
- Detects your platform and surfaces the correct installer.
- Always installs from the **latest published release** and highlights its changelog.
- Presents a polished details screen with stats, README, and developer info.

The repository is currently private while the core is being stabilized, but it will be public and fully open source under the Apache 2.0 license very soon.

---

## 🚀 Features

- **Smart discovery**
  - Home sections for “Popular”, “Recently Updated”, and “New” projects.
  - Only repos with valid installable assets are shown.
  - Platform‑aware topic scoring so Android/desktop users see relevant apps first.

- **Latest‑release installs**
  - Fetches `/releases/latest` for each repo.
  - Shows only assets from the latest release.
  - Single “Install latest” action, plus an expandable list of all installers for that release.

- **Rich details screen**
  - App name, version, “Install latest” button.
  - Stars, forks, open issues, contributors.
  - Rendered README content (“About this app”).
  - Latest release notes (body) with markdown formatting.
  - List of installers with platform labels and file sizes.

- **Cross‑platform UX**
  - Android: opens APK downloads and hands off to the package installer.
  - Desktop (Windows/macOS/Linux): downloads to a configurable location and opens the file.

- **GitHub login & rate‑limit friendly**
  - Sign‑in with GitHub to use the user’s own 5,000 req/hour quota.
  
---

## 🔍 How does my app appear in GithubStore?

GithubStore does not use any private indexing or manual curation rules.  
Your project can appear automatically if it follows these conditions:

1. **Public repository on GitHub**
   - Visibility must be `public`.

2. **At least one published release**
   - Created via GitHub Releases (not only tags).
   - The latest release must not be a draft or prerelease.

3. **Installable assets in the latest release**
   - The latest release must contain at least one asset file with a supported extension:
     - Android: `.apk`
     - Windows: `.exe`, `.msi`
     - macOS: `.dmg`, `.pkg`
     - Linux: `.deb`, `.rpm`
   - GithubStore ignores GitHub’s auto‑generated source artifacts (`Source code (zip)` / `Source code (tar.gz)`).

4. **Discoverable by search / topics**
   - Repositories are fetched via the public GitHub Search API.
   - Topic, language, and description help the ranking:
     - Android apps: topics like `android`, `mobile`, `apk`.
     - Desktop apps: topics like `desktop`, `gui`, `app`, `compose-desktop`, `electron`.
   - Having at least a few stars makes it more likely to appear under Popular/Updated/New sections.

If your repo meets these conditions, GithubStore can find it through search and show it automatically—no manual submission required.

---

## 🧭 How GithubStore works (high‑level)

1. **Search**
   - Uses GitHub’s `/search/repositories` endpoint with platform‑aware queries.
   - Applies simple scoring based on topics, language, and description.
   - Filters out archived repos and those with too few signals.

2. **Release + asset check**
   - For candidate repos, calls `/repos/{owner}/{repo}/releases/latest`.
   - Checks the `assets` array for platform‑specific file extensions.
   - If no suitable asset is found, the repo is excluded from results.

3. **Details screen**
   - Repository info: name, owner, description, stars, forks, issues, contributors.
   - Latest release: tag, published date, body (changelog), assets.
   - README: loaded from the default branch and rendered as “About this app”.

4. **Install flow**
   - When the user taps “Install latest”:
     - Picks the best matching asset for the current platform.
     - Streams the download.
     - Delegates to the OS installer (APK installer on Android, default handler on desktop).

---

## 🧱 Tech stack

- **Language & Platform**
  - Kotlin Multiplatform (Android + JVM Desktop)
  - Compose Multiplatform UI

- **Networking & Data**
  - Ktor `HttpClient` for GitHub REST API.
  - kotlinx.serialization for JSON models.
  - Kotlinx.coroutines + Flow for async + streaming search results.

- **Architecture**
  - Clean modular design with `core` (domain/models) and feature modules.
  - Repository pattern for data access.
  - ViewModel/state holder per screen (platform‑specific wrapper around shared logic).

- **Auth & Deep‑links**
  - GitHub OAuth (Device Code flow).

---

## ✅ Pros / Why use GithubStore?

- **No more hunting through GitHub releases**  
  See only repos that actually ship binaries for your platform.

- **Always the latest release**  
  Installs are guaranteed to come from the latest published release; the changelog you see is exactly what you’re installing.

- **Uniform experience across platforms**  
  Same UI and logic for Android and desktop, with platform‑native install behavior.

- **Open source & extensible**  
  Written in KMP with a clear separation between networking, domain logic, and UI—easy to fork, extend, or adapt.

---

## 📦 Getting started (WIP)

> The repository is currently private while the core is being finalized.  
> Once it goes public, this section will include:
> - Installation instructions for Android APK and desktop builds.
> - Instructions for building from source.
> - Contribution guidelines and issue templates.

---

## 🤝 Contributing

Planned contribution areas once the repo is public:

- New platform heuristics (better detection via topics/languages).
- Additional asset type support.
- Improved search filters and ranking.
- UI/UX enhancements, accessibility, and localization.

Issues and pull requests will be very welcome once the repository is open.

---

## 📄 License

GithubStore will be released under the **Apache License, Version 2.0**.

