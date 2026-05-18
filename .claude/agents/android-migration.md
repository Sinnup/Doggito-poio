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

### Phase 2 — AGP 9 Upgrade
**Branch:** `chore/upgrade-agp-9`
**Risk:** Medium — breaking DSL changes, Kotlin/Java version gates  
**Depends on:** Phase 1 complete (versions centralized)  
**Skill:** `build/agp/agp-9-upgrade`

**Known Gradle 8 carry-over from Phase 1:** `kotlin-kapt` and `kotlin-parcelize` are still
applied via legacy short-form IDs in submodules because Gradle 8.0 cannot reconcile versioned
catalog plugin aliases with plugins already on the classpath without version metadata.
After upgrading to AGP 9 + Kotlin 2.x in this phase, switch those two plugins to
`alias(libs.plugins.kotlin.kapt)` and `alias(libs.plugins.kotlin.parcelize)` and verify
no `@Parcelize` or KAPT compilation errors.

**Goal:** Upgrade the build toolchain to AGP 9+, Kotlin 2.x, Java 17, and apply the
new AGP DSL. Run the Android Studio AGP Upgrade Assistant first (8.1 → 8.x stable),
then apply the skill for the 8.x → 9 boundary.

**Steps:**
1. **Upgrade Assistant (user action):** In Android Studio, run
   *Tools → AGP Upgrade Assistant* to reach the latest AGP 8.x stable. Confirm sync passes.
2. **Invoke skill:** `Skill({ skill: "build/agp/agp-9-upgrade" })`
3. **Upgrade Kotlin:** `2.x` in `libs.versions.toml`.
4. **Upgrade Java:** Set `jvmTarget = "17"` and `sourceCompatibility = JavaVersion.VERSION_17`
   in all modules (currently `1.8` everywhere).
5. **Unify compileSdk:** Set `compileSdk = 35` in all modules.
6. **Apply new AGP DSL:** `namespace` block, updated `buildFeatures`, etc. per skill guide.
7. **Remove deprecated gradle.properties flags** (per skill step 6):
   `android.builtInKotlin`, `android.newDsl`, `android.uniquePackageNames`,
   `android.enableAppCompileTimeRClass`

**Verification:**
```
./gradlew help
./gradlew build --dry-run
```

---

### Phase 3 — KAPT → KSP
**Branch:** `chore/migrate-kapt-to-ksp`
**Risk:** Low-medium — mechanical swap, one module at a time  
**Depends on:** Phase 2 complete  
**Skill:** Covered in `build/agp/agp-9-upgrade` Step 4

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

### Phase 4 — Navigation 3
**Branch:** `feat/migrate-to-navigation-3`
**Risk:** High — major refactor touching all modules and all screens  
**Depends on:** Phase 3 complete  
**Skill:** `navigation/navigation-3`

**Goal:** Replace the three-layer navigation system with a single Navigation 3 + Compose
graph. This enables deletion of several legacy Activity classes.

**Current navigation inventory:**
- Fragment Navigation Component: `app/src/main/res/navigation/auth_nav_graph.xml`
  (used in auth flow — `LoginActivity`)
- Activity-based navigation (in `MainActivity`):
  - `MainActivity` → `LoginActivity`
  - `MainActivity` → `DogListActivity`
  - `MainActivity` → `DogDetailComposeActivity`
  - `MainActivity` → `SettingsActivity`
  - `MainActivity` → `WholeImageActivity`
- Compose Navigation 2: `DogListScreen`, `DogDetailScreen`, `AuthScreen` (internal nav)

**Target structure (Navigation 3):**
```
NavDisplay (root)
├── AuthGraph (conditional — shown when user not logged in)
│   ├── LoginScreen
│   └── SignUpScreen
└── MainGraph (shown when user logged in)
    ├── DogListScreen
    ├── DogDetailScreen
    ├── SettingsScreen
    └── CameraScreen (camera capture + ML recognition)
```

**Steps:**
1. Invoke skill: `Skill({ skill: "navigation/navigation-3" })`
2. Add Navigation 3 dependency to `libs.versions.toml`.
3. Define `NavKey` sealed classes for each destination in `core` module
   (shared across feature modules).
4. Implement conditional navigation for auth flow (replaces nav graph XML).
5. Migrate `DogListScreen`, `DogDetailScreen` to Navigation 3 destinations.
6. Migrate `AuthScreen` / `LoginScreen` / `SignUpScreen` to Navigation 3 destinations.
7. Create `SettingsScreen` composable (currently `SettingsActivity` + XML).
8. Wire `CameraScreen` into the graph.
9. Delete: `LoginActivity`, `DogListActivity`, `DogDetailActivity`,
   `DogDetailComposeActivity`, `SettingsActivity`, `WholeImageActivity`,
   `auth_nav_graph.xml`.
10. Remove Fragment Navigation Component dependencies from `libs.versions.toml`.

---

### Phase 5 — Full Compose Migration (XML → Compose)
**Branch:** `feat/migrate-views-to-compose`
**Risk:** Medium — scoped to specific layouts, visual parity required  
**Depends on:** Phase 4 complete (Activity shells no longer needed)  
**Skill:** `jetpack-compose/migration/migrate-xml-views-to-jetpack-compose`

**Goal:** Eliminate all remaining XML layouts and DataBinding usage.
After Phase 4, the only remaining XML surfaces will be in the `camera` module.

**Known XML targets:**
- `camera` module: CameraX preview layout (DataBinding-based)
- Any remaining Activity layouts in `app` (verify after Phase 4 deletions)

**Steps:**
1. Invoke skill: `Skill({ skill: "jetpack-compose/migration/migrate-xml-views-to-jetpack-compose" })`
2. Migrate camera preview XML layout to a Compose `AndroidView` wrapper around
   `PreviewView` (CameraX composable interop pattern).
3. Remove DataBinding from `app/build.gradle` and `camera/build.gradle`:
   ```groovy
   // Remove these blocks:
   buildFeatures { dataBinding true }
   ```
4. Remove DataBinding dependency from `libs.versions.toml`.
5. Sync and verify no DataBinding imports remain.

---

### Phase 6 — MVI Architecture
**Branch:** `refactor/migrate-to-mvi`
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

### Phase 7 — Polish
**Branch:** `chore/apply-polish-and-edge-to-edge`
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
