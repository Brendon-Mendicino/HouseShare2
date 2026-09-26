# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Project

HouseShare2 is a single-module Android app (`:app`, package `lol.terabrendon.houseshare2`) for
sharing house management (shopping lists, expense splitting, groups) between housemates. Kotlin 2.3,
Jetpack Compose + Material3 (expressive APIs), Hilt, Room, DataStore, Retrofit, Navigation3. minSdk
24, JDK 21. It is an offline-first client of a remote REST backend (`BuildConfig.BASE_URL`, set per
build type in `app/build.gradle.kts`).

## Commands

All via the Gradle wrapper from the repo root:

```sh
./gradlew :app:assembleDebug              # build debug APK
./gradlew :app:installDebug               # install on connected device/emulator
./gradlew :app:testDebugUnitTest          # JVM unit tests (app/src/test)
./gradlew :app:testDebugUnitTest --tests "lol.terabrendon.houseshare2.domain.model.MoneyTest"          # single class
./gradlew :app:testDebugUnitTest --tests "*ExpenseBalanceMapperTest.map with empty*"                    # single test (backtick names use spaces; glob them)
./gradlew :app:connectedDebugAndroidTest  # instrumented tests (app/src/androidTest), needs a device — includes Room migration tests
./gradlew :app:lint                       # Android lint
```

Unit tests use JUnit4 + Truth + mockito-kotlin. `gradle.properties` sets `ksp.incremental=false`
deliberately (KSP bug); leave it.

Release builds need `keystore.properties` and `app/google-services.json` (both gitignored). Debug
builds still need `google-services.json` for the Firebase plugin.

`scripts/trans.py` (deps in `requirements.txt`, `.venv`) machine-translates `values/strings.xml`
into other locales via googletrans and prints the result; it does not write files.

## Architecture

Three layers under `lol.terabrendon.houseshare2`: `data` → `domain` → `presentation`, wired with
Hilt modules in `di/`.

### Error handling: `Result` everywhere, no exceptions across layers

The whole codebase uses `com.michael-bull.kotlin-result`. Errors are a sealed hierarchy in
`domain/error/`:

- `RootError` ← `DataError` ← `LocalError` (SQLite) | `RemoteError` (one case per HTTP status +
  `NoConnection`); `RootError` ← `FormError`.
- Type aliases: `NetResult<T>` (remote), `LocalResult<T>` (Room), `DataResult<T>` (either; what
  repositories return).

How each boundary produces them:

- **Retrofit** interfaces return `NetResult<T>` directly.
  `data/remote/api/ResultCallAdapterFactory` + `ConvertResponse.kt` map HTTP codes/IOExceptions to
  `RemoteError`. The OkHttp client has `followRedirects(false)`; a 3xx to `/login` is treated as
  `Unauthorized`.
- **Room** calls are wrapped in `localSafe { }` / `transactionSafe(db) { }` /
  `transactionBinding(db) { ... bind() }` from `data/local/util/QueryWrappers.kt`, which catch
  SQLite exceptions into `LocalError` (and rethrow `CancellationException`).
- **Repositories** compose these with `getOrElse { return Err(it) }` and return `DataResult`.
- **ViewModels** destructure `val (_, err) = repo.call()` and forward errors to
  `SnackbarController.sendError(err)`.
- `RootError.toUiText()` in `presentation/util/Errors.kt` maps every error to a localized `UiText`.
  Adding a new error variant means adding a branch there (the `when`s are exhaustive).
- Code launched in the application-wide scope (`HouseShareApplication.applicationScope`) may throw
  `RootException(err)`; the scope's `CoroutineExceptionHandler` turns it into a snackbar.

### Data layer (offline-first)

- `data/entity/` Room entities (+ `composite/` relation classes), `data/local/dao/`,
  `data/local/database/HouseShareDatabase.kt` (schema exported to `app/schemas/`, currently v3;
  `fallbackToDestructiveMigration` is on, so bump `version` when changing entities and add the new
  schema JSON).
- `data/remote/dto/` Gson DTOs and `data/remote/api/` Retrofit interfaces. Gson is configured with a
  custom `OffsetDateTime` adapter (`domain/typeadapter/`).
- `data/repository/` interfaces + `*Impl`, bound in `di/RepositoryModule.kt`. Reads are `Flow`s from
  Room; writes go remote first, then upsert locally; `refreshByGroupId`-style methods pull remote
  pages and reconcile (upsert + delete missing) into Room. `GroupRepository.findOrFetchMember` is
  the lazy-load path for members referenced by other entities.
- `data/local/preferences/` is a kotlinx-serialization DataStore (`UserData`) exposed through
  `UserDataRepository`. It stores the **navigation back stack**, logged user id, selected group id,
  T&C/analytics consent and theme. `UserDataRepository.update(Update.X(...))` is the only write API.
- `domain/mapper/` holds all `toModel()` / `toEntity()` / `toDto()` / `toForm()` extension functions
  between entities, DTOs, domain models (`domain/model/`) and form states.
- Auth is cookie-session based: a persisted `CookieJar` (`SharedPrefCookieStore`) plus
  `CsrfInterceptor` are shared by the API Retrofit (`BASE_URL/api/v1/`) and the separate auth
  Retrofit (`BASE_URL`).

### Navigation (Navigation3, back stack persisted in DataStore)

- Routes are `@Serializable` `NavKey`s: `MainNavigation` (Loading/Login/Legal) and its subclass
  `HomepageNavigation` (Shopping, Billing, Groups, ..., plus form/detail routes).
  `MainNavigation.asResource()` maps each route to its top-bar title string.
- `Navigator<MainNavigation>` (`presentation/navigation/NavigatorImpl.kt`, owned by `MainViewModel`)
  is the single navigation API: `navigate` / `replace` / `pop`. It writes the back stack to
  `UserDataRepository`, and the exposed `backStack` flow overrides it with Legal/Loading/Login when
  terms aren't accepted, login status is unknown, or the user is logged out. Login status is polled
  from the server every 2 minutes; offline with a saved user counts as logged in. Navigating to a
  `topLevelRoutes` entry resets the stack.
- `HouseShareMain.kt` hosts the `NavDisplay`; each feature contributes an
  `EntryProviderScope<MainNavigation>.xxxNavigation(navigator)` extension (e.g.
  `screen/shopping/ShoppingNavigation.kt`). Route-parameterised ViewModels use Hilt assisted
  factories: `hiltViewModel<VM, VM.Factory>(creationCallback = { it.create(key) })`.
- `rememberSharedViewModelStoreNavEntryDecorator` (`presentation/util/`) lets sibling entries (e.g.
  multi-step group form) share a ViewModel.
- `DeepLinkActivity` handles group invite links (`AcceptInviteUseCase`).

### Presentation conventions

- Screens live in `presentation/screen/<feature>/`, ViewModels in `presentation/vm/`. Screens send
  sealed `XxxEvent`s to `viewModel.onEvent(event)`; one-shot results come back through a `Channel`
  -backed `uiEvents` flow of `XxxUiEvent` consumed with `ObserveAsEvent`.
- Scaffold chrome is configured from inside screens via CompositionLocal-backed stack managers (
  `presentation/provider/`): `RegisterTopBarConfig(config, route = X::class)` and the FAB
  equivalent (`FabManager` / `FabConfig.Fab` / `FabConfig.Toolbar`). `StateManager` keeps a stack so
  the most recently registered, currently-active route wins.
- `SnackbarController` is a global `Channel` — any coroutine can `sendEvent` / `sendRes` /
  `sendError`; `HouseShareMain` renders it.
- User-facing strings are `UiText` (`Res` / `Dyn` / `Multi`), resolved with `.text()` in
  composables. Strings go in `res/values/strings.xml` (default locale is `en` via
  `resources.properties`; `values-it` is the maintained translation).
- Selected group and logged user come from `GetSelectedGroupUseCase` / `GetLoggedUserUseCase` /
  `GetLoggedMemberUseCase` (`domain/usecase/`, plain `@Inject` classes with
  `operator fun invoke()`).

### Forms (aformvalidator, KSP-generated)

Form states in `domain/form/` are `@FormState` data classes annotated with `@NotBlank`, `@Min`,
`@ToNumber`, `@DependsOn`, etc. (custom validators like `@IsTrue` live in `util/Valiadator.kt`). KSP
generates `toValidator()`, per-field `.update(value)`, `.errors`, and `.toData()`; ViewModels hold
`MutableStateFlow(FormState().toValidator())` and on submit check `formState.errors.firstOrNull()`
before mapping `toData().toModel()`. Text inputs are kept as `String` fields (`amountStr`) with
derived typed properties (`amount`).

### Logging

Use `Timber` (`Timber.i("methodName: message %s", arg)` style with the calling function name as
prefix). In release builds only `ERROR` goes to Crashlytics, and only if analytics consent is on (
`util/Logger.kt`).

## Conventions

- Commit messages are prefixed with the target version, e.g. `v1.0.3: added X` (version in
  `app/build.gradle.kts` `versionName`).
- Dependency versions live in `gradle/libs.versions.toml`; `okHttp` is pinned to what Retrofit
  requires (see comment there).
- Material3 is an alpha with expressive APIs; most screens
  `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`.
