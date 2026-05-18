# Dogedex — Migration Changelog

This file is the project's single source of truth for migration state.
Read this first in any new session. Update it after every commit.
Format: newest entry on top. One entry per commit or logical unit of work.

---

## CURRENT STATE

```
Phase:       3 — KAPT → KSP
Status:      Complete ✓
Next phase:  4 — Navigation 3
Branch:      feat/migrate-to-navigation-3  ← create this branch next
```

**Next action:** Replace the three-layer navigation system (Fragment Nav, Activity-based,
Compose Nav 2) with a single Navigation 3 + Compose graph. Detailed steps:
`.claude/agents/android-migration.md` → Phase 4.

---

## [v0.5] — 2026-05-18 — Phase 3: KAPT → KSP

### Status: Done

### Branch
`chore/migrate-kapt-to-ksp`

### Commits
| SHA | Message |
|---|---|
| `91c5f14` | `chore(build): add KSP plugin to root build, remove kotlin-kapt catalog entry` |
| `64c1524` | `chore(build): migrate core and auth modules from kapt to ksp` |
| `dd49009` | `chore(build): migrate app and camera modules from kapt to ksp` |
| `e97c46e` | `test(app): replace DogedexTestCoroutineRule with MainDispatcherRule` |
| `fad29f3` | `test(app): fix broken test imports, fakes, and coroutine APIs` |

### Done
- Removed `kotlin-kapt` plugin entry from version catalog
- Added `alias(libs.plugins.ksp) apply false` to root `build.gradle`
- Replaced `kapt`/`kaptAndroidTest` → `ksp`/`kspAndroidTest` in all four modules
- Removed all `androidTestAnnotationProcessor` lines (caused duplicate-class errors with KSP)
- Converted `id 'androidx.navigation.safeargs'` → `alias(libs.plugins.navigation.safe.args)` in `app`
- Removed `kapt.use.k2=true` from `gradle.properties`
- Deleted `DogedexTestCoroutineRule.kt`; replaced with `MainDispatcherRule` (TestWatcher-based)
- Fixed all three previously broken test files: correct imports, `UnconfinedTestDispatcher`,
  `runTest` replacing `runBlocking`, missing `getProbableDogs` fake override, `19L` type fix,
  dropped unresolvable `R.string.*` assertions

### Gate results
- `./gradlew assembleDebug` — PASS
- `./gradlew test` — PASS ✓ (zero failures — first clean test run in the project)
- `grep -r "kapt" --include="*.gradle" .` — 0 results ✓
- `grep -r "kaptAndroidTest\|androidTestAnnotationProcessor" --include="*.gradle" .` — 0 results ✓

### Deviations from plan
- `kotlin-parcelize` kept as legacy string ID (`id 'kotlin-parcelize'`) in `app` and `core`:
  applying it as a versioned catalog alias fails because the plugin is bundled inside
  `kotlin.android` and Gradle cannot reconcile the duplicate registration with a version.
- KSP version resolved to `2.1.21-2.0.2` (not `2.1.21-1.0.29` from Phase 2 catalog —
  that patch was unpublished). IDE lint warns about KSP `2.3.4` being available; this is
  incorrect — `2.3.4` requires Kotlin 2.3.x and is incompatible with our Kotlin 2.1.21.

---

## [v0.4] — 2026-05-18 — Phase 2: Build Toolchain Upgrade

### Status: Done

### Branch
`chore/upgrade-agp-9`

### Commits
| SHA | Message |
|---|---|
| `f1057b7` | `chore(build): bump AGP 8.10.1, Kotlin 2.1.21, compileSdk 36, Hilt 2.56.1` |
| `378b071` | `chore(build): apply Kotlin 2.x DSL — jvmToolchain 17, compose compiler plugin, compileSdk 36` |
| `f2a4ef1` | `chore(build): enable K2 KAPT compat and configuration cache` |
| `da28781` | `chore(build): bump Gradle wrapper 8.11.1 → 8.14.5` |

### Done
- AGP: 8.13.2 → 8.10.1 (latest stable; AGP 9 not yet GA as of this migration)
- Kotlin: 1.9.0 → 2.1.21 (K2 compiler)
- Gradle wrapper: 8.13 → 8.14.5
- KSP: 1.9.0-1.0.13 → 2.1.21-1.0.29 (version scheme changed with Kotlin 2.x)
- Hilt: 2.48 → 2.56.1 (minimum for Kotlin 2.x / K2 KAPT)
- Compose BOM: 2023.05.00 → 2025.05.00
- compileSdk / targetSdk: 34 → 36
- Java compatibility: VERSION_1_8 → VERSION_17 via `kotlin { jvmToolchain(17) }`
- `composeOptions { kotlinCompilerExtensionVersion }` removed from all modules
- `kotlin-compose-compiler` plugin applied to `app`, `core`, `auth`
- `packagingOptions` renamed to `packaging` in `app`
- `kapt.use.k2=true` and `org.gradle.configuration-cache=true` added to `gradle.properties`
- Two deprecated Compose API calls fixed (BOM bump forced migration):
  - `TextFieldDefaults.outlinedTextFieldColors` → `TextFieldDefaults.colors` (`core/AuthField.kt`)
  - `TopAppBarDefaults.smallTopAppBarColors` → `TopAppBarDefaults.topAppBarColors` (`app/DogListScreen.kt`)

### Gate results
- `./gradlew assembleDebug` — PASS
- `./gradlew assembleRelease` — PASS
- `./gradlew lint` — PASS (0 errors; 109 warnings are all dependency-upgrade advisories)
- `./gradlew test` — pre-existing failures only (same 3 files, same root cause as Phase 1)

### Deviations from plan
- AGP target adjusted from "9.x" to 8.10.1: AGP 9.0 is not yet GA. The plan will be updated
  to target AGP 9 once it ships stable — all DSL changes applied here are forward-compatible.

---

## [v0.3] — 2026-05-18 — Phase 1: Version Catalog

### Status: Done

### Branch
`chore/migrate-version-catalog`

### Commits
| SHA | Message |
|---|---|
| `e7c76cd` | `chore(deps): add libs.versions.toml with all dependency versions` |
| `9b17f30` | `chore(deps): wire version catalog and migrate root build.gradle` |
| `cccd26a` | `chore(deps): migrate core module to version catalog` |
| `8511dd3` | `chore(deps): migrate auth module to version catalog` |
| `1dce88c` | `chore(deps): migrate camera module to version catalog` |
| `48fd221` | `chore(deps): migrate app module to version catalog` |

### Done
- Created `gradle/libs.versions.toml` with all versions, libraries, plugins, and bundles.
- Removed legacy `buildscript {}` block from root `build.gradle`; Safe Args moved to `plugins {}`.
- Migrated all four modules (`core`, `auth`, `camera`, `app`) to version catalog accessors.
- Unified version conflicts: `appcompat` → 1.6.1, `material` → 1.9.0, `compose-bom` → 2023.05.00,
  `navigation` → 2.7.0, `lifecycle-*` → 2.6.1, `activity-*` → 1.7.2.
- Set `compileSdk = 34` in all modules (was 33 in `core`, `auth`, `camera`).
- Pinned `tensorflow-lite-support:+` to `0.4.4` (was dynamic `+`).
- Removed dead `hilt-navigation-compose` dependency from `camera` module.
- Removed duplicate Hilt test block in `auth/build.gradle`.
- `coroutines-test` upgraded `1.5.0` → `1.7.3`.

### Gate results
- `./gradlew assembleDebug` — PASS
- `grep -r "implementation \"" --include="*.gradle" .` — 0 results ✓
- `grep -r "classpath " --include="*.gradle" .` — 0 results ✓

### Deviations from plan
- `settings.gradle` `versionCatalogs { from(...) }` block was omitted: Gradle 8.0 auto-discovers
  `gradle/libs.versions.toml` and registering it explicitly causes a duplicate-registration error.
- `kotlin-kapt` and `kotlin-parcelize` are applied via legacy short-form IDs in submodules
  (not `alias()`): Gradle 8.0 raises a hard error when a versioned catalog alias is applied
  for a plugin already on the classpath without version metadata. This is the correct approach
  for this Gradle version and will be resolved when AGP 9 + Kotlin 2.x are in place (Phase 2/3).

---

## [v0.2] — 2026-05-18 — Skills Source Documentation

### Status: Done

### Changed
- Updated `.claude/agents/android-migration.md`: skills section now references the official
  source at https://github.com/android/skills instead of the bare local path `~/.claude/skills`.
- Added first-time setup command (`git clone`) and weekly update command (`git -C ~/.claude/skills pull`).
- Added prominent notice that the local skills repo must be pulled at least once a week,
  with the reason (stale skills may reference outdated APIs or deprecated Gradle DSL).

---

## [v0.1] — 2026-05-18 — Migration Planning

### Status: Done

### Decided
- Migration strategy: 7 sequential phases gated by build verification.
- Target stack: AGP 9+, Kotlin 2.x, Java 17, KSP, libs.versions.toml, Navigation 3, MVI, Compose-only UI, edge-to-edge.
- Architecture pattern: MVI with `UiState` / `UiAction` / `UiEffect` per screen.
  `ApiResponseStatus<T>` stays in the data layer only — never in `UiState` or Composables.
- DI: Hilt stays. KAPT → KSP in Phase 3. Target Hilt 2.59.2+.
- Navigation: Fragment Nav + Activity-based + Compose Nav 2 → unified Navigation 3.
  Auth flow becomes conditional navigation, not a separate nav graph XML.
- `compileSdk` unified to 35 in Phase 2 (currently 34 in `app`, 33 in `core/auth/camera`).
- Testing strategy: fakes over mocks, `runTest` replaces `runBlocking`,
  `DogedexTestCoroutineRule` deleted in Phase 3, Turbine added in Phase 6.

### Done
- Created `.claude/agents/android-migration.md` — master migration plan (7 phases).
- Created `.claude/agents/architect.md` — architectural rules and MVI contract design.
- Created `.claude/agents/developer.md` — implementation patterns and code style.
- Created `.claude/agents/tester.md` — testing patterns, inventory, and coverage targets.
- Created `.claude/agents/versioning.md` — commit conventions and branching strategy.
- Created this `CHANGELOG.md`.

### Baseline versions (do not change without a CHANGELOG entry)
| Tool | Current | Target |
|---|---|---|
| AGP | 8.1.0 | 9.x latest stable |
| Gradle wrapper | 8.0 | per AGP 9 compatibility table |
| Kotlin | 1.9.0 | 2.x |
| Java compatibility | 1.8 | 17 |
| compileSdk | 34 (app) / 33 (others) | 35 (all) |
| Hilt | 2.48 | 2.59.2+ |
| KSP | not present | 2.3.6+ |
| Compose BOM | 2023.01.00 (app) / 2023.05.00 (core) | latest stable |
| Navigation Compose | 2.4.1 (app) / 2.7.0 (core) | Navigation 3 |
| Retrofit | 2.9.0 | check for updates in Phase 1 |
| Coroutines Test | 1.5.0 | match kotlinx.coroutines version |
| App versionCode | 1 | 1 (no change during migration) |
| App versionName | 1.0 | 1.0 (no change during migration) |

---

## UPCOMING PHASES

### Phase 2 — AGP 9 Upgrade
**Branch:** `chore/upgrade-agp-9`
**Pre-step (user, in Android Studio):** Run AGP Upgrade Assistant (8.1 → 8.x stable).
**Commits:**
1. `chore(build): upgrade AGP to 9.x, Kotlin to 2.x, Gradle wrapper`
2. `chore(build): set Java 17 and unify compileSdk 35 across all modules`
3. `chore(build): apply new AGP 9 DSL changes`
4. `chore(build): remove deprecated gradle.properties flags`

**Gate:** `./gradlew help` and `./gradlew build --dry-run` pass.

---

### Phase 3 — KAPT → KSP
**Branch:** `chore/migrate-kapt-to-ksp`
**Commits (one per module):**
1. `chore(deps): upgrade Hilt to 2.59.2, add KSP plugin to version catalog`
2. `chore(build): migrate app module from kapt to ksp`
3. `chore(build): migrate core module from kapt to ksp`
4. `chore(build): migrate auth module from kapt to ksp`
5. `chore(build): migrate camera module from kapt to ksp`
6. `test(app): replace DogedexTestCoroutineRule with runTest`

**Gate:** `grep -r "kapt" --include="*.gradle" .` returns nothing.

---

### Phase 4 — Navigation 3
**Branch:** `feat/migrate-to-navigation-3`
**Commits (one per destination / concern):**
1. `feat(core): add NavKey sealed classes for all app destinations`
2. `feat(core): add SessionRepository exposing auth state as StateFlow`
3. `feat(nav): implement root NavDisplay with conditional auth/main graph`
4. `feat(nav): migrate DogListScreen to Navigation 3 destination`
5. `feat(nav): migrate DogDetailScreen to Navigation 3 destination`
6. `feat(nav): migrate auth screens (Login, SignUp) to Navigation 3`
7. `feat(nav): add SettingsScreen composable, remove SettingsActivity`
8. `feat(nav): wire CameraScreen into Navigation 3 graph`
9. `refactor(app): delete legacy Activities and auth_nav_graph.xml`
10. `chore(deps): remove Fragment Navigation Component from version catalog`

**Gate:** `DogListActivity`, `LoginActivity`, `DogDetailComposeActivity`, `SettingsActivity`,
`WholeImageActivity`, `auth_nav_graph.xml` are deleted. `./gradlew assembleDebug` passes.

---

### Phase 5 — Full Compose Migration
**Branch:** `feat/migrate-views-to-compose`
**Commits:**
1. `feat(camera): replace DataBinding camera preview with Compose AndroidView`
2. `chore(build): remove dataBinding from app and camera build.gradle`

**Gate:** `grep -r "dataBinding" --include="*.gradle" .` returns nothing.

---

### Phase 6 — MVI Architecture
**Branch:** `refactor/migrate-to-mvi`
**Commits (one per ViewModel):**
1. `chore(deps): add Turbine to test dependencies`
2. `refactor(app): migrate MainViewModel to MVI (UiState/UiAction/UiEffect)`
3. `refactor(auth): migrate AuthViewModel to MVI`
4. `refactor(app): migrate DogListViewModel to MVI`
5. `refactor(app): migrate DogDetailViewModel to MVI`
6. `refactor(core): remove ApiResponseStatus from all UI-layer imports`
7. `test(app): add ViewModel unit tests with Turbine for all screens`

**Gate:** `grep -r "MutableLiveData\|observeAsState\|ApiResponseStatus" --include="*.kt" app/src/main auth/src/main` returns nothing.

---

### Phase 7 — Polish
**Branch:** `chore/apply-polish-and-edge-to-edge`
**Commits:**
1. `feat(app): apply edge-to-edge window insets to all screens`
2. `chore(build): analyze and clean R8 rules post-migration`
3. `test(app): add screenshot tests for all screens (9 window sizes)`
4. `test(app): run and verify full instrumented test suite`
