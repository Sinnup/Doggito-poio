---
name: android-migration
description: >-
  Migration agent for Dogedex. Analyze code against the migration plan, check
  progress on any phase, or execute a specific step. Covers: libs.versions.toml,
  AGP 9+ upgrade, KAPT→KSP, Navigation 3, full Compose migration, MVI
  architecture (UiState/UiAction/UiEffect), and edge-to-edge support.
---

# Dogedex — Android Architecture Migration Agent

You are a senior Android architect executing a multi-phase migration of the Dogedex project.
Before taking any action, read the relevant source files to understand the current state.
Never modify more than one phase at a time. Always verify a build succeeds before moving
to the next phase.

---

## Project Snapshot (baseline at migration start)

**Modules:** `app`, `core`, `auth`, `camera`
**AGP:** 8.1.0 → target 9.x latest stable
**Kotlin:** 1.9.0 → target 2.x
**Java compatibility:** 1.8 → target 17
**compileSdk:** 34 (app) / 33 (core, auth, camera) → unify to 35
**Annotation processing:** KAPT → target KSP
**DI:** Hilt 2.48 → target 2.59.2+
**Navigation:** hybrid (Fragment Nav Component + Activity-based + Compose Nav 2) → target Navigation 3
**State management:** mixed LiveData + Compose mutableStateOf → target StateFlow + UiState
**Compose BOM:** inconsistent (2023.01.00 / 2023.05.00) → target latest stable BOM
**DataBinding:** enabled in `app` and `camera` → remove after Compose migration complete

---

## Available Skills

Skills are sourced from the **official Modern Android Development skills repository**:
**https://github.com/android/skills**

> **Weekly update required.**
> Run `git -C ~/.claude/skills pull` at least once a week to keep your local copy current.
> Skill definitions are actively maintained by the Android team — stale skills may reference
> outdated APIs, deprecated Gradle DSL, or superseded library versions.
> If a skill invocation produces unexpected output, pull first and retry before debugging further.

### Setup (first time)

```bash
git clone https://github.com/android/skills ~/.claude/skills
```

### Keeping up to date (weekly minimum)

```bash
git -C ~/.claude/skills pull
```

### Skills used in this migration

| Skill name | Phase | Purpose |
|---|---|---|
| `build/agp/agp-9-upgrade` | 2, 3 | AGP 8.x → 9 upgrade steps and DSL migration |
| `navigation/navigation-3` | 4 | Nav Component + Activity nav → Navigation 3 |
| `jetpack-compose/migration/migrate-xml-views-to-jetpack-compose` | 5 | XML layouts → Compose |
| `testing/testing-setup` | 6 | Test infrastructure for ViewModel/UiState unit and UI tests |
| `system/edge-to-edge` | 7 | Edge-to-edge display modernization |
| `performance/r8-analyzer` | 7 | Post-migration R8/ProGuard optimization |
| `camera/camera1-to-camerax` | — | Camera API (CameraX already in use — low priority) |

To invoke a skill, use: `Skill({ skill: "<skill-name>" })`

---

## Migration Phases

### Phase 1 — Version Catalog (`libs.versions.toml`) ✓ COMPLETE
**Branch used:** `chore/migrate-version-catalog`
**Risk:** Low — purely additive, zero behavior change  
**Must complete before:** every other phase

**Goal:** Centralize all dependency versions into `gradle/libs.versions.toml` and remove
hardcoded versions from all `build.gradle` files.

**Files to read first:**
- `build.gradle` (root)
- `app/build.gradle`
- `core/build.gradle`
- `auth/build.gradle`
- `camera/build.gradle`
- `settings.gradle`

**Known inconsistencies to resolve:**
- Compose BOM: `2023.01.00` (app) vs `2023.05.00` (core) → unify to latest
- Navigation Compose: `2.4.1` (app) vs `2.7.0` (core) → unify to latest
- Lifecycle: `2.3.1` / `2.5.1` / `2.6.1` across modules → unify to latest
- Activity Compose: `1.6.1` → upgrade to latest
- Retrofit: `2.9.0` → check for updates
- Coroutines Test: `1.5.0` → upgrade to match kotlinx.coroutines version

**Steps:**
1. Enable version catalog in `settings.gradle`:
   ```groovy
   dependencyResolutionManagement {
       versionCatalogs {
           libs {
               from(files("gradle/libs.versions.toml"))
           }
       }
   }
   ```
2. Create `gradle/libs.versions.toml` with `[versions]`, `[libraries]`, `[plugins]`, `[bundles]` sections.
3. Replace all inline `build.gradle` dependency declarations with catalog accessors (`libs.xxx`).
4. Replace plugin declarations in root `build.gradle` and module `build.gradle` files with catalog plugin aliases.
5. Sync and verify: `./gradlew help`

**Completion check:** `grep -r "implementation \"" --include="*.gradle" .` returns no results.

---

### Phase 2 — Build Toolchain Upgrade ✓ COMPLETE
**Branch used:** `chore/upgrade-agp-9`

**What landed:** AGP 8.10.1, Kotlin 2.1.21 (K2), Gradle 8.14.5, KSP 2.1.21-1.0.29,
Hilt 2.56.1, Compose BOM 2025.05.00, compileSdk/targetSdk 36, Java 17 via
`kotlin { jvmToolchain(17) }`, `kotlin-compose-compiler` plugin replacing `composeOptions`,
`kapt.use.k2=true` + `org.gradle.configuration-cache=true` in gradle.properties.
Note: AGP 9 not yet GA — will retarget once it ships stable.
`kotlin-kapt` and `kotlin-parcelize` still use legacy string IDs — convert to `alias()` in Phase 3.

---

### Phase 3 — KAPT → KSP ✓ COMPLETE
**Branch used:** `chore/migrate-kapt-to-ksp`

**What landed:** KSP 2.1.21-2.0.2 in all four modules; `kapt`/`kaptAndroidTest` fully
removed; `androidTestAnnotationProcessor` removed; `DogedexTestCoroutineRule` deleted and
replaced with `MainDispatcherRule`; all three broken test files fixed; `./gradlew test`
passes with zero failures for the first time. `kotlin-parcelize` kept as legacy string ID
(catalog alias registration conflicts with `kotlin.android` bundle).

**Goal:** Replace KAPT annotation processing with KSP across all modules.
KSP is incremental and significantly faster than KAPT.

**Modules using KAPT (all 4):** `app`, `core`, `auth`, `camera`
**Affected dependencies:** Hilt compiler, Moshi codegen (if any), Room (if added later)

**Steps:**
1. Upgrade Hilt to `2.59.2+` in `libs.versions.toml`.
2. Add KSP plugin: `com.google.devtools.ksp` version `2.3.6+` to `libs.versions.toml` and
   root `build.gradle` plugins block.
3. In each module's `build.gradle`:
   - Remove: `apply plugin: 'kotlin-kapt'`
   - Add: `apply plugin: 'com.google.devtools.ksp'`
   - Replace: `kapt "com.google.dagger:hilt-android-compiler:..."` →
     `ksp "com.google.dagger:hilt-android-compiler:..."`
   - Replace: `kaptAndroidTest` → `kspAndroidTest` (in `app` module test config)
4. Sync and run: `./gradlew assembleDebug`

**Completion check:** `grep -r "kapt" --include="*.gradle" .` returns no results
(except possibly legacy-kapt configuration if needed).

---

### Phase 4 — Navigation 3 ✓ COMPLETE
**Branch used:** `feat/migrate-to-navigation-3`

**What landed:** `kotlinx-serialization-json 1.7.3` + `navigation 2.8.9` added to
version catalog. `NavKey` sealed interface with six `@Serializable` destinations in
`core`. `SessionRepository` singleton (`isLoggedIn: StateFlow<Boolean>`) + `SessionManager`
interface for Hilt. `MainViewModel` converted from `MutableLiveData` → `StateFlow<MainUiState>`.
`DogedexNavHost` — single `NavHost` reactive to `isLoggedIn`. `CameraScreen`,
`SettingsScreen` composables. `MainActivity` rewritten to `ComponentActivity`.
Deleted: five Activity classes, `AuthNavDestinations.kt`, `AuthScreen.kt`,
`auth_nav_graph.xml`, all `activity_*.xml`/`fragment_*.xml` layouts, `DogAdapter.kt`,
`dog_list_item.xml`. Removed `dataBinding` from `app/build.gradle`.
Note: `camera/build.gradle` DataBinding removal deferred to Phase 5.

---

### Phase 5 — Full Compose Migration (XML → Compose) ✓ COMPLETE
**Branch used:** `feat/migrate-views-to-compose`

**What landed:** `buildFeatures { dataBinding true }` removed from `camera/build.gradle`.
Unused `appcompat` and `material` dependencies removed from `camera`. Dead
`camera/src/main/res/values/strings.xml` deleted (3 unreferenced permission strings).
No XML-to-Compose migration was needed: `camera/layout/` was already empty and
`AndroidView { PreviewView }` was wired in `CameraScreen.kt` during Phase 4.
DataBinding is now absent from all modules. `libs.versions.toml` required no changes.

---

### Phase 6 — MVI Architecture ✓ COMPLETE
**Branch:** `refactor/migrate-to-mvi`

**What landed:** `UiState`/`UiAction`/`UiEffect` contracts added for all four screens.
`DogListViewModel`, `DogDetailViewModel`, `MainViewModel`, and `AuthViewModel` fully migrated
to MVI — `MutableLiveData` and `mutableStateOf` removed, replaced with `StateFlow<UiState>`
and `Channel<UiEffect>`. `StringResolverModule` added to `core` for unit-testable string
resolution. All composables updated to `collectAsStateWithLifecycle()` + single `handleAction`
lambda. `AuthViewModelTest` rewritten with 5 Turbine-based tests. Zero `MutableLiveData` or
`observeAsState` references remain outside tests. `ApiResponseStatus` confined to Repositories
and ViewModels only — no Composable imports it.

**Risk:** Medium — logic restructure in all ViewModels, no UI visual change  
**Depends on:** Phase 5 complete  
**Skill:** `testing/testing-setup` (for wiring unit tests to each ViewModel after migration)

**Goal:** Replace the current mixed state model (`MutableLiveData`, `mutableStateOf`,
`ApiResponseStatus<T>` leaking into UI) with the MVI pattern: each screen owns an
immutable `UiState`, dispatches typed `UiAction` intents to its ViewModel, and receives
one-time `UiEffect` side effects via a `Channel`. No external library required — built
entirely on Kotlin coroutines and `StateFlow`.

#### The MVI contract

Every screen gets three types, defined alongside its ViewModel:

```kotlin
// 1. Immutable state snapshot — drives all UI rendering
data class DogListUiState(
    val dogs: List<Dog> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// 2. User intents — the only way the UI talks to the ViewModel
sealed class DogListUiAction {
    data object LoadDogs : DogListUiAction()
    data class OnDogSelected(val dog: Dog) : DogListUiAction()
}

// 3. One-time side effects — navigation triggers, snackbars, etc.
sealed class DogListUiEffect {
    data class NavigateToDogDetail(val dog: Dog) : DogListUiEffect()
}
```

The ViewModel exposes exactly two streams and one entry point:

```kotlin
@HiltViewModel
class DogListViewModel @Inject constructor(
    private val dogTasks: DogTasks,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(DogListUiState())
    val uiState: StateFlow<DogListUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<DogListUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<DogListUiEffect> = _uiEffect.receiveAsFlow()

    fun handleAction(action: DogListUiAction) {
        when (action) {
            is DogListUiAction.LoadDogs -> loadDogs()
            is DogListUiAction.OnDogSelected -> viewModelScope.launch {
                _uiEffect.send(DogListUiEffect.NavigateToDogDetail(action.dog))
            }
        }
    }
    // ...
}
```

The Composable is fully stateless — it only reads and dispatches:

```kotlin
@Composable
fun DogListScreen(viewModel: DogListViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is DogListUiEffect.NavigateToDogDetail -> { /* trigger nav */ }
            }
        }
    }

    DogListContent(
        uiState = uiState,
        onAction = viewModel::handleAction
    )
}
```

#### ApiResponseStatus elimination

`ApiResponseStatus<T>` is a network-layer sealed class currently leaking into ViewModels
and Composables. Under MVI it must stay in the Repository only. The ViewModel maps it:

```kotlin
// Inside ViewModel — Repository returns ApiResponseStatus, ViewModel absorbs it
when (val result = dogTasks.getDogCollection()) {
    is ApiResponseStatus.Loading -> _uiState.update { it.copy(isLoading = true) }
    is ApiResponseStatus.Success -> _uiState.update {
        it.copy(isLoading = false, dogs = result.data)
    }
    is ApiResponseStatus.Error -> _uiState.update {
        it.copy(isLoading = false, error = result.messageId.toString())
    }
}
```

After migration, no Composable or Navigation destination should import
`ApiResponseStatus`.

#### Screen-by-screen migration map

| Screen | ViewModel file | Current state model | Target UiState fields |
|---|---|---|---|
| Dog list | `dogList/DogListViewModel.kt` | `mutableStateOf<List<Dog>>` + `ApiResponseStatus` | `dogs`, `isLoading`, `error` |
| Dog detail | `dogDetail/DogDetailViewModel.kt` | `mutableStateOf<Dog?>` + `ApiResponseStatus` | `dog`, `isLoading`, `hasDogBeenAdded`, `error` |
| Auth (login/signup) | `auth/.../AuthViewModel.kt` | `mutableStateOf<ApiResponseStatus?>` + `mutableStateOf<User?>` | `isLoading`, `error` + effects for navigation |
| Main / camera | `main/MainViewModel.kt` | `MutableLiveData<ApiResponseStatus<Dog>>` | `isLoading`, `recognizedDog`, `error`, `requiresLogin` |

#### Steps

1. Create `UiState`, `UiAction`, `UiEffect` types for each screen, co-located with their
   ViewModel (not in `core` — these are screen-specific contracts).
2. Migrate `MainViewModel` first (it is the only one using `MutableLiveData`; completing
   it proves the LiveData elimination strategy before touching the Compose-state VMs).
3. Migrate `AuthViewModel`, `DogListViewModel`, `DogDetailViewModel` in that order.
4. Update each corresponding Composable to use `collectAsStateWithLifecycle()` and
   a single `onAction` lambda instead of multiple callback parameters.
5. Delete all `MutableLiveData` imports and `observeAsState()` calls project-wide.
6. Delete `ApiResponseStatus` references from all files outside the `api/` package.
7. Invoke the testing skill to wire unit tests:
   `Skill({ skill: "testing/testing-setup" })`
   - Unit-test each ViewModel by dispatching `UiAction` instances and asserting
     `uiState` emissions using `kotlinx.coroutines.test.runTest`.
   - Replace `DogedexTestCoroutineRule` with `runTest` (available since coroutines 1.6+).
   - Add Compose UI behavior tests per screen (step 9 of the testing skill).

**Completion check:**
```
grep -r "MutableLiveData\|observeAsState\|ApiResponseStatus" \
  --include="*.kt" \
  --exclude-dir=api \
  app/src auth/src camera/src
```
Returns no results.

---

### Phase 7 — Polish ✓ COMPLETE
**Branch:** `chore/apply-polish-and-edge-to-edge`

**What landed:** `enableEdgeToEdge()` added to `MainActivity`; `adjustResize` set in
`AndroidManifest.xml`; `Scaffold(contentWindowInsets = WindowInsets.safeDrawing)` applied
to `LoginScreen` and `SignUpScreen` for IME safety; `contentPadding` on `LazyVerticalGrid`
in `DogListScreen`; `navigationBarsPadding()` on FABs in `CameraScreen` and `DogDetailScreen`.
`minifyEnabled true` + `shrinkResources true` enabled in app release build; stack-trace
attributes uncommented in `app/proguard-rules.pro`; no-op `proguardFiles` removed from
library modules; unused `navigation-safe-args` plugin removed from version catalog;
`org.gradle.parallel=true` enabled. Screenshot tests and `connectedAndroidTest` are
out of scope for this migration.

**Risk:** Low  
**Depends on:** Phase 6 complete

**Steps:**
1. **Edge-to-edge:** Invoke `Skill({ skill: "system/edge-to-edge" })`. Applies window
   inset handling to all Compose screens.
2. **R8 analysis:** Invoke `Skill({ skill: "performance/r8-analyzer" })`. Review
   ProGuard rules, remove unnecessary `-keep` rules added for DataBinding/KAPT.
3. **Screenshot tests:** Per the testing skill (step 8), add screen-level screenshot
   tests for `DogListScreen`, `DogDetailScreen`, `LoginScreen`, `SignUpScreen`
   at 9 window size combinations (compact/medium/expanded × compact/medium/expanded).
4. **Verify instrumented tests:** Run full `connectedAndroidTest` suite.

---

### Phase 8 — AGP 9 Upgrade
**Branch:** `chore/upgrade-agp-9`
**Risk:** Medium — Gradle wrapper major bump + built-in Kotlin removes a plugin from every module  
**Depends on:** Phase 7 complete  
**Skill:** `build/agp/agp-9-upgrade`

**Goal:** Upgrade from AGP 8.13.2 to AGP 9.x latest stable, adopt built-in Kotlin (removes
the `kotlin-android` plugin from all four modules), upgrade Gradle wrapper to 9.1.0+, and
align the dependency chain (Hilt 2.59.2+, KSP matching the new KGP, Kotlin 2.2.10+).

#### Pre-flight checks

Before starting, run:
```bash
git -C ~/.claude/skills pull          # ensure skill is current
./gradlew help -q                     # confirm clean baseline
grep -r "android.r8.integratedResourceShrinking\|android.enableNewResourceShrinker" gradle.properties
```
The `grep` must return nothing — AGP 9 throws a hard error if either of those flags is set.

#### Version targets (`libs.versions.toml`)

| Key | Current | Target |
|---|---|---|
| `agp` | 8.13.2 | 9.x latest stable |
| `kotlin` | 2.1.21 | 2.2.10+ (AGP 9 bundles 2.2.10 as minimum) |
| `ksp` | 2.1.21-2.0.2 | match new kotlin version (e.g. `2.2.10-2.0.2`) |
| `hilt` | 2.56.1 | 2.59.2+ (required by AGP 9 skill Step 1) |

#### Steps

**Step 1 — Version catalog** (`libs.versions.toml`)
1. Bump `agp`, `kotlin`, `ksp`, `hilt` to targets above.
2. Remove the `kotlin-android` plugin entry from `[plugins]` — it is replaced by AGP built-in Kotlin:
   ```toml
   # DELETE this line:
   kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
   ```
3. Verify: `./gradlew help -q`

**Step 2 — Gradle wrapper** (`gradle/wrapper/gradle-wrapper.properties`)
- AGP 9.0 requires **Gradle 9.1.0 minimum** (hard requirement — Gradle 8.x will refuse).
- Update `distributionUrl` to the latest stable Gradle 9.x release.
- Verify: `./gradlew --version`

**Step 3 — Root `build.gradle`**
- Remove `alias(libs.plugins.kotlin.android) apply false` — the plugin no longer exists in the catalog.
- The remaining plugins (`android.application`, `android.library`, `hilt.android`, `ksp`, `kotlin.compose.compiler`, `kotlin.serialization`) stay.

**Step 4 — Module `build.gradle` files** (all four: `app`, `core`, `auth`, `camera`)
For each module:
1. Remove `alias(libs.plugins.kotlin.android)` — AGP 9 built-in Kotlin replaces it.
2. Remove the `kotlin { jvmToolchain(17) }` block from inside `android {}` — with built-in
   Kotlin, `jvmTarget` defaults to `android.compileOptions.targetCompatibility` automatically.
   The `compileOptions` block (`sourceCompatibility`/`targetCompatibility VERSION_17`) stays.
3. `app/build.gradle` only — fix `kotlin-parcelize` legacy ID:
   - Remove `id 'kotlin-parcelize'`
   - Add `alias(libs.plugins.kotlin.parcelize)`
   - This was blocked during Phase 3 by a conflict with `kotlin-android`; that conflict is gone now.

**Step 5 — `gradle.properties` audit**
AGP 9 changes several property defaults and removes others. Verify:
- `android.r8.integratedResourceShrinking` — must NOT be set (causes hard error).
- `android.enableNewResourceShrinker.preciseShrinking` — must NOT be set (causes hard error).
- `android.defaults.buildfeatures.aidl` and `android.defaults.buildfeatures.renderscript` — must NOT be set (removed in AGP 9).
- After migration succeeds, clean up any of the following opt-out flags the Upgrade
  Assistant may have inserted: `android.builtInKotlin`, `android.newDsl`,
  `android.uniquePackageNames`, `android.enableAppCompileTimeRClass`.

**Step 6 — Build verification**
```bash
./gradlew help -q                        # DSL check
./gradlew build --dry-run                # task graph check
./gradlew :app:assembleDebug             # full debug compile
./gradlew :app:assembleRelease           # R8 release compile
./gradlew :app:testDebugUnitTest         # unit tests
```

**Step 7 — Commit**
```
chore(deps): upgrade AGP to 9.x and migrate to built-in Kotlin
```

#### Known Dogedex-specific risks

| Risk | Mitigation |
|---|---|
| Hilt incompatible with built-in Kotlin at lower versions | Hilt 2.59.2+ is tested with AGP 9 built-in Kotlin |
| `kotlin-parcelize` alias conflict (was an issue in Phase 3) | Resolved — `kotlin-android` is removed first |
| `kotlin-compose-compiler` plugin — still applies `kotlin.plugin.compose` | This is NOT `kotlin-android`; it stays and works with built-in Kotlin |
| `kotlin-serialization` plugin — still applies `kotlin.plugin.serialization` | Same — separate from `kotlin-android`; stays |
| `KSP` still needs explicit version if higher than AGP's bundled default | Keep explicit version in catalog; must match KGP |
| Groovy DSL build files (`.gradle` not `.gradle.kts`) | AGP 9 supports both; no forced migration to Kotlin DSL |

**Completion check:**
```bash
./gradlew :app:assembleRelease          # must succeed
grep "kotlin.android" app/build.gradle  # must return nothing
grep "kotlin.android" core/build.gradle # must return nothing
grep "kotlin.android" auth/build.gradle # must return nothing
grep "kotlin.android" camera/build.gradle # must return nothing
grep "id 'kotlin-parcelize'" app/build.gradle # must return nothing
```

---

## How to Use This Agent

**To check migration progress:**
> "Check which phases of the android-migration plan are complete."

The agent will read the current `build.gradle` / `libs.versions.toml` / source files and
report status per phase without modifying anything.

**To execute a specific phase:**
> "Execute Phase 1 of the android-migration plan."

The agent will read relevant files, invoke the appropriate skill if needed, make changes
one file at a time, and verify before proceeding.

**To analyze a specific module:**
> "Analyze the camera module against the android-migration plan."

The agent will read the module's build files and source, then report what migration
work remains for that module across all phases.

---

## Invariants — Never Violate These

- Never skip a phase to get to a later one. Each phase's verification step is a gate.
- Never run `./gradlew clean` as a verification step (wastes time; `help` and `--dry-run` suffice).
- Never add `android.disallowKotlinSourceSets=false` to `gradle.properties`.
- Never use Python scripts for any migration step.
- Never modify more than one module's `build.gradle` in a single commit during Phase 1-3.
- Always read the current state of a file before editing it.
- `fruse-key.keystore` and `keyvalues.txt` must never be committed or referenced in migration code.
