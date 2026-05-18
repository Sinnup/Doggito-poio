# Dogedex — Migration Changelog

This file is the project's single source of truth for migration state.
Read this first in any new session. Update it after every commit.
Format: newest entry on top. One entry per commit or logical unit of work.

---

## CURRENT STATE

```
Phase:       0 — Planning
Status:      Complete
Next phase:  1 — Version Catalog (libs.versions.toml)
Branch:      chore/phase-1-version-catalog  ← create this branch next
```

**Next action:** Create `gradle/libs.versions.toml` centralizing all versions from
the four `build.gradle` files. Then update each module one at a time.
Detailed steps: `.claude/agents/android-migration.md` → Phase 1.

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

### Phase 1 — Version Catalog
**Branch:** `chore/phase-1-version-catalog`
**Commits (one per module after catalog creation):**
1. `chore(deps): add libs.versions.toml with all current dependency versions`
2. `chore(deps): migrate root build.gradle to version catalog`
3. `chore(deps): migrate app module to version catalog`
4. `chore(deps): migrate core module to version catalog`
5. `chore(deps): migrate auth module to version catalog`
6. `chore(deps): migrate camera module to version catalog`

**Gate:** `grep -r "implementation \"" --include="*.gradle" .` returns nothing.

---

### Phase 2 — AGP 9 Upgrade
**Branch:** `chore/phase-2-agp9`
**Pre-step (user, in Android Studio):** Run AGP Upgrade Assistant (8.1 → 8.x stable).
**Commits:**
1. `chore(build): upgrade AGP to 9.x, Kotlin to 2.x, Gradle wrapper`
2. `chore(build): set Java 17 and unify compileSdk 35 across all modules`
3. `chore(build): apply new AGP 9 DSL changes`
4. `chore(build): remove deprecated gradle.properties flags`

**Gate:** `./gradlew help` and `./gradlew build --dry-run` pass.

---

### Phase 3 — KAPT → KSP
**Branch:** `chore/phase-3-ksp`
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
**Branch:** `feat/phase-4-navigation3`
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
**Branch:** `feat/phase-5-compose`
**Commits:**
1. `feat(camera): replace DataBinding camera preview with Compose AndroidView`
2. `chore(build): remove dataBinding from app and camera build.gradle`

**Gate:** `grep -r "dataBinding" --include="*.gradle" .` returns nothing.

---

### Phase 6 — MVI Architecture
**Branch:** `refactor/phase-6-mvi`
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
**Branch:** `chore/phase-7-polish`
**Commits:**
1. `feat(app): apply edge-to-edge window insets to all screens`
2. `chore(build): analyze and clean R8 rules post-migration`
3. `test(app): add screenshot tests for all screens (9 window sizes)`
4. `test(app): run and verify full instrumented test suite`
