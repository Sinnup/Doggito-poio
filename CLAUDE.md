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

## Versioning

Use **Conventional Commits** (`type(scope): imperative summary`). Types: `feat`, `fix`,
`refactor`, `chore`, `build`, `test`, `docs`. Scopes include `build`, `deps`, `app`, `core`,
`camera`. One concern per commit; each commit must build (`./gradlew assembleDebug`). Branch
names must include a change-type word (e.g. `migration`, `update`, `refactor`) alongside the
topic area. Update `CHANGELOG.md` for user-visible changes. See `.claude/agents/versioning.md`
for the full protocol.

## Tooling in this repo

- Sub-agents (`.claude/agents/`): `architect` (design specs, no code), `developer`
  (implementation), `tester`, `versioning`, `android-migration`.
- Skill: `/release` publishes the app to Google Play (see `.claude/commands/release.md`).
