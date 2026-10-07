# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project

**Dogedex** is an offline-first Android app that identifies dog breeds from the camera
using an on-device TensorFlow Lite classifier, then lets the user build a collection of
recognized dogs. All dog data ships with the app (assets copied to internal storage on
first launch) and is stored locally in Room — there is no backend.

- Package: `com.espert.dogedex` · minSdk 24 · Java 17 · Kotlin 2.3 · AGP 9
- UI: Jetpack Compose + Material 3, single-Activity, Navigation Compose
- DI: Hilt · Async: Coroutines + StateFlow · Persistence: Room

## Modules

| Module | Responsibility |
|---|---|
| `:app` | Screens & UI (Camera/Main, DogList, DogDetail, Settings), navigation host, app-level ViewModels, repositories, DI wiring. |
| `:camera` | Camera + ML only: `Classifier`, `ClassifierRepository`, `DogRecognition`, ML DI modules. No UI. |
| `:core` | Shared foundation: Room DB (`DogedexDatabase`, DAOs, entities, mappers), domain `model` (`Dog`, `ResponseStatus`), Compose `ui/theme`, reusable `composables` (LoadingWheel, ErrorDialog, AuthField, BackNavigationIcon), `navigation` keys/type utils, asset copy helpers, DI modules. |

Dependency direction: `:app` → `:camera` → `:core`, and `:app` → `:core`. Never make `:core`
or `:camera` depend on `:app`. Put anything shared by two modules in `:core`.

## Architecture — MVI

Each screen follows a strict MVI contract (see `dogList/`, `dogDetail/`, `main/`):

- **UiState** — immutable data class held in a `StateFlow`, collected via
  `collectAsStateWithLifecycle()`. The composable is a pure function of state.
- **UiAction** — sealed interface of user intents; the screen calls
  `viewModel.handleAction(action)`. The ViewModel is the only place that mutates state.
- **UiEffect** — sealed interface for one-shot events (navigation, etc.), emitted through a
  channel/flow and consumed in a `LaunchedEffect`. Never model navigation as state.

Screen composables split into a stateful entry (`XScreen`, takes the `hiltViewModel()`) and a
stateless `XContent` (takes `uiState` + lambdas) so Content is previewable and testable.

Responsive layout uses `WindowSizeClass` with the extensions in
`app/.../ui/WindowSizeUtils.kt` (`isCompact` / `isMedium` / `isExpanded`).

## Theme

Colors, type scale, and the `DogedexTheme` composable live in `:core` under `ui/theme/`.
The brand identity is warm amber/brown (`primary = #825500`). Prefer `MaterialTheme.colorScheme`
tokens over `colorResource`/hardcoded `Color(...)` in composables. Edge-to-edge is enabled in
`MainActivity`; the theme only controls status-bar icon appearance (do not set `statusBarColor`).

## Preferences & onboarding

User preferences persist via Jetpack DataStore behind `UserPreferencesRepository`
(`:core/.../preferences/`), provided by Hilt (`PreferencesModule`). `MainActivity` gates the
first-run `OnboardingScreen` (`app/.../onboarding/`) on the persisted `hasSeenOnboarding` flag,
keeping the splash on screen until the flag loads, then swapping to the walkthrough or the
`DogedexNavHost`. Skip and completion both persist the flag so onboarding never shows again.

## Build & test

```bash
./gradlew assembleDebug          # build the whole app (debug)
./gradlew :core:assembleDebug    # build a single module
./gradlew installDebug           # install on a connected device/emulator
./gradlew testDebugUnitTest      # JVM unit tests (JUnit, Mockito, Robolectric, Turbine)
./gradlew connectedDebugAndroidTest  # instrumented tests (Hilt + Compose UI)
```

- SDK levels come from `gradle/libs.versions.toml` (`compileSdk`/`targetSdk`/`minSdk`) — change
  them there, not in the module `build.gradle` files. Dependencies are managed via the version
  catalog and `[bundles]`.
- Instrumented tests use `CustomTestRunner` (Hilt). Prefer **fakes over mocks**; use `runTest` +
  Turbine for Flow/StateFlow assertions and the Compose test APIs for UI.
- **CI** (`.github/workflows/android.yml`) builds the **debug variant only**
  (`assembleDebug` + `testDebugUnitTest`). Never use `./gradlew build` there — it also builds
  release, which needs signing credentials.
- Release signing reads `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`,
  `RELEASE_KEY_PASSWORD` from the gitignored `local.properties`. `app/build.gradle` only
  configures the `release` signing config when they exist, so debug builds and CI work without
  them. Release builds (`:app:bundleRelease`, `scripts/release.sh`) are local-only.

## Versioning

Use **Conventional Commits** (`type(scope): imperative summary`). Types: `feat`, `fix`,
`refactor`, `chore`, `build`, `test`, `docs`. Scopes include `build`, `deps`, `app`, `core`,
`camera`. One concern per commit; each commit must build (`./gradlew assembleDebug`). Branch
names must include a change-type word (e.g. `migration`, `update`, `refactor`) alongside the
topic area. Update `CHANGELOG.md` for user-visible changes. See `.claude/agents/versioning.md`
for the full protocol.

## Public assets

- **Play Store listing**: `https://play.google.com/store/apps/details?id=com.espert.dogedex`
- **Portfolio**: `https://sinnup.github.io` (repo `Sinnup/Sinnup.github.io`, local clone at
  `/Users/sinue/Documents/portfolio`). Doggito has a project card in `index.html` and its own page
  at `doggito/index.html` (live at `https://sinnup.github.io/doggito/`), with media in
  `doggito/media/` (9 numbered phone-frame captures in `doggito/media/funcionalidades/NN.jpg`, 1000px
  JPGs made from the originals in `portfolio/screenshots/` of this repo, which stay untracked; when
  adding captures, renumber sequentially and add a card in the page). Keep version, minSdk, features and tech stack there in sync with the app.
- **Code repo**: `https://github.com/Sinnup/Doggito-poio`

## Feature-complete workflow

When the user says a feature is done ("feature complete", "feature completado", "listo",
"terminado", or similar), run this whole flow without asking again, delegating to the sub-agents
where useful:

1. **`CHANGELOG.md`** — add an entry for the change.
2. **Update context** — refresh every file that describes the change if it is now stale: this
   `CLAUDE.md`, `.claude/agents/*.md`, `.claude/commands/*.md`, and the memory files in
   `~/.claude/projects/-Users-sinue-Documents-Doggito-poio/memory/` (+ `MEMORY.md` index).
3. **Atomic commits** — one logical change per commit, Conventional Commits format, explicit
   paths staged, no secrets or untracked working notes (`NEXT_STEPS.md`, `.kotlin/`).
4. **Commit to `main` and push** — `git push origin main`; if the work is on a feature branch,
   merge it into `main` first. Verify the working tree is clean afterwards.
5. **Publish publicly when user-visible** — update the portfolio card and the Doggito page in
   `/Users/sinue/Documents/portfolio`, commit there to `main` and push to
   `Sinnup/Sinnup.github.io` so the change is live on GitHub Pages.

## Tooling in this repo

- Sub-agents (`.claude/agents/`): `architect` (design specs, no code), `developer`
  (implementation), `tester`, `versioning`, `android-migration`.
- Skill: `/release` publishes the app to Google Play (see `.claude/commands/release.md`).
