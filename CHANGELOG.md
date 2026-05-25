# Dogedex — Migration Changelog

This file is the project's single source of truth for migration state.
Read this first in any new session. Update it after every commit.
Format: newest entry on top. One entry per commit or logical unit of work.

---

## CURRENT STATE

```
Phase:       Post-v1.2 network removal
Status:      Complete ✓
Next action: Prepare release APK and upload to Play Console
Branch:      main
```

**Context:** App is now fully offline-first. All network-related code (Retrofit, OkHttp,
ApiService) and dependencies have been removed. App uses bundled assets and Room DB.

---

## [v1.2] — 2026-05-23 — Network Removal

### Status: Done

### Branch
`main`

### Done

- **Network Removal**: completely removed Retrofit, OkHttp, and Moshi dependencies.
- **API Package Deleted**: deleted `core/api` package including `ApiService`, DTOs, and network interceptors.
- **Offline-First**: refactored `DogRepository` and ViewModels to use only Room database.
- **Model Migration**: moved `ResponseStatus` to `core.model` and updated project-wide imports.
- **Clean up**: removed network-related ProGuard rules and constants.

### Gate results
- `./gradlew assembleDebug` — PASS
- `./gradlew test` — PASS ✓

---

## [v1.1] — 2026-05-23 — Authentication Removal

### Status: Done

### Branch
`main`

### Done

- **Auth Removal**: completely removed the `:auth` module and all related code (Login, SignUp screens, ViewModels, and repository).
- **Navigation**: simplified `DogedexNavHost` to start directly at `CameraScreen`. Removed all login-state reactive logic.
- **Session Cleanup**: deleted `SessionManager` and `SessionRepository` from `core`.
- **API Cleanup**: removed login/signup endpoints from `ApiService` and unused auth DTOs.
- **Strings**: removed all authentication-related strings from both English and Spanish resources.
- **Settings**: simplified `SettingsScreen` to only include the Privacy Policy.

### Gate results
- `./gradlew assembleDebug` — PASS
- `./gradlew test` — PASS ✓

---

## [v1.0] — 2026-05-23 — Release Polish & AGP 9 Migration

### Status: Done

### Branch
`main`

### Done

- **AGP 9 Compatibility**: migrated legacy `applicationVariants` to modern `androidComponents` API in `app/build.gradle`.
- **SDK Update**: unified `compileSdk` and `targetSdk` to 35 (Android 15) in `libs.versions.toml`.
- **Splash Screen**: implemented `androidx.core:core-splashscreen` API. Added `Theme.App.Starting` and called `installSplashScreen()` in `MainActivity`.
- **Material 3 Migration**: fixed `MostProbableDogsDialog` to use Material 3 `AlertDialog` parameters (confirmButton), resolving build errors.
- **Theme & UI Polish**: updated `Theme.Dogedex` to inherit from `Theme.Material3.DayNight.NoActionBar` and configured transparent system bars for full edge-to-edge support.
- **Lint Cleanup**: removed unused imports, added trailing commas, fixed deprecated `hiltViewModel` calls, and improved code style in `CameraScreen`, `DogedexNavHost`, and `MostProbableDogsDialog`.

### Gate results
- `./gradlew assembleDebug` — PASS
- `./gradlew test` — PASS ✓

---

## [post-v0.9] — 2026-05-22 — Incremental improvements

### Status: Done

### Branch
`main`

### Done

- **`app/build.gradle`**: `targetSdk` bumped 36 → 37
- **`MostProbableDogsDialog`**: each list item now displays a 56 dp thumbnail on the right,
  loaded from local storage (`filesDir/images/<imageUrl>.jpg` via Coil) — consistent with
  the image display in `DogDetailScreen`

### Gate results
- `./gradlew assembleDebug` — PASS
- `./gradlew test` — PASS ✓

---

## [v0.9] — 2026-05-22 — Camera & Navigation Improvements

### Status: Done

### Branch
`main`

### Done

#### Navigation fixes
- **DogedexNavHost**: `composable<DogDetailKey>` now calls `it.toRoute<DogDetailKey>()`
  and passes `dog`, `probableDogIds`, and `isRecognition` as explicit parameters to
  `DogDetailScreen`, replacing the previous implicit route extraction that silently dropped
  the custom `Dog` NavType
- **DogDetailScreen**: signature updated to accept `dog: Dog`, `probableDogIds: List<String>`,
  and `isRecognition: Boolean` as explicit parameters; null-dog guard removed (dog is
  guaranteed non-null from the route)
- **DogDetailViewModel**: `savedStateHandle.toRoute<DogDetailKey>()` was missing
  `typeMap = mapOf(typeOf<Dog>() to DogType)`, causing silent null deserialization for the
  custom `Dog` NavType; fixed to include the typeMap and simplified to a non-nullable
  `dogDetailKey`

#### Camera improvements
- **CameraScreen**: tap-to-focus implemented using `pointerInput` / `detectTapGestures`
  on the `AndroidView` modifier — the correct Compose approach, since `setOnTouchListener`
  is overridden by Compose's gesture layer; `Camera` instance stored in state;
  `ImageAnalysis` now calls `setTargetRotation`
- **ClassifierRepository**: fixed portrait-mode dog detection — `rotationDegrees` was
  commented out, so the ML model was receiving sideways frames in portrait orientation;
  now reads `imageProxy.imageInfo.rotationDegrees` and applies `Matrix.postRotate` before
  passing the bitmap to TFLite

#### UI improvements
- **DogListScreen**: `TopAppBar` replaced with `CenterAlignedTopAppBar` to center the
  title; `DogGridItem` non-collected branch wrapped in `Box(contentAlignment = Center)` to
  prevent the index number from overflowing at large font sizes
- **BackNavigationIcon**: added `tint: Color = Color.Black` parameter for icon color control

### Gate results
- `./gradlew assembleDebug` — PASS
- `./gradlew test` — PASS ✓

---

## [v0.8] — 2026-05-21 — Offline-first Local Data Source

### Status: Done

### Branch
`refactor/migrate-to-local-data-source`

### Commits
| SHA | Message |
|---|---|
| `e98e526` | `refactor(core): rename ApiResponseStatus to ResponseStatus and fix response package` |
| `87009c8` | `feat(core): add Room database layer and AssetCopyHelper infrastructure` |
| `707715e` | `feat(app): offline-first data layer — bypass login, seed Room DB from bundled assets` |
| `2cdd155` | `test(app): update test files for local data source and bundle dog assets` |

### Done
- **`ApiResponseStatus` → `ResponseStatus`**: renamed sealed class and moved from
  legacy `com.fruse.dogedex.api.responses` package to the canonical
  `com.fruse.dogedex.core.api.responses`; all API response DTOs (Auth, Dog, Default,
  DogList, DogListResponse, DogResponse, UserResponse) package declarations updated;
  `ApiService.getDogBYMlId` renamed to `getDogByMlId`
- **Room database** (`core` module): `DogEntity`, `DogDao`, `DogedexDatabase` (v1),
  `DogEntityMapper`, `DatabaseModule` Hilt provider; Room 2.8.4 added to catalog and
  `core/build.gradle` with schema directory configured
- **AssetCopyHelper** (`core`): utility that recursively copies an assets subfolder
  to `context.filesDir`; `AssetModule` Hilt provider
- **ImageRepository** (`app`): copies `assets/images/` to local storage on startup
- **DogTasks / DogRepository** (`app`): added five DB-backed method variants —
  `insertAllDogs`, `getDogCollectionDB`, `addDogToUserDB`, `getDogBYMlIdDB`,
  `getProbableDogsDB`
- **MainViewModel**: `setUpAssets(context)` reads `dogs.json` from assets, bulk-inserts
  all 118 breeds into Room, then copies dog images to `filesDir`; ML recognition
  result now resolved via `getDogBYMlIdDB`
- **MainActivity**: instantiates `MainViewModel` via `viewModels()` and calls
  `setUpAssets(this)` in `onCreate`; `DOGS_JSON_FILE = "dogs.json"` constant
- **DogedexNavHost**: `isLoggedIn` hardcoded to `true` so the app opens directly on
  `CameraScreen` while the remote auth API is unavailable
- **CameraScreen**: take-photo FAB is only shown when ML confidence ≥ 70%
- **DogListViewModel / DogDetailViewModel**: switched to `*DB()` repository methods
- **Assets bundled**: `app/src/main/assets/dogs.json` (118 breeds) and
  `app/src/main/assets/images/` (118 JPEG files, ~4.6 MB)
- **Test renames**: `DogListScreenTest` → `DogEntityListScreenTest`,
  `DogRepositoryTest` → `DogEntityRepositoryTest`,
  `DogListViewModelTest` → `DogEntityListViewModelTest`

### Gate results
- `./gradlew assembleDebug` — PASS
- `./gradlew test` — PASS ✓
