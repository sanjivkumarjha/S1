# S96 — Snaper AI Assistant (Android)

## Project
- applicationId: `com.aistudio.snapertech.aiassistant`, AGP **namespace**: `com.example`
- Path: `/workspace/project/S96`
- Repo: `sanjivkumarjha/S96`

## Build environment setup (this sandbox resets between sessions)
- Install: `sudo apt-get install -y openjdk-21-jdk-headless unzip`
- JDK: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`
- Android SDK at `$HOME/android-sdk`. If wiped, reinstall cmdline-tools:
  `curl -sL -o /tmp/c.zip "https://dl.google.com/android/repository/commandlinetools-linux-12266719_latest.zip" && unzip -q /tmp/c.zip -d $HOME/android-sdk/cmdline-tools && mv $HOME/android-sdk/cmdline-tools/cmdline-tools $HOME/android-sdk/cmdline-tools/latest`
  then `yes | sdkmanager --licenses` and `sdkmanager "platforms;android-35" "build-tools;35.0.0" "platform-tools"` (gradle auto-installs build-tools;34 if needed)
- `local.properties` must contain `sdk.dir=/home/openhands/android-sdk` (gitignored)
- Build: `./gradlew :app:assembleDebug --no-daemon` (run in background + poll log; foreground cap ~1080s)

## Key architecture notes
- **namespace vs applicationId**: activity-alias / service component names live under the
  namespace (`com.example`), NOT the applicationId. `setComponentEnabledSetting` must build
  `ComponentName(namespace, "$namespace.$alias")`, never use `context.packageName`.
- **Face verification**: `FaceEnrollmentManager` stores a SHA-256 hash (avalanche → char-match
  is ~constant). Compare the **raw feature buckets** with tolerance instead; store them in
  the signature file under a `FEATURES:` line.
- **Voice**: `VoiceAssistantService` owns a `VoiceAssistantManager`; the hands-free loop uses
  `onRecognitionCycleComplete` to re-listen. `VoiceInteractionState` singleton bridges live
  speak/listen state to the home avatar + Dynamic Island.
- Foreground services: FGS type `microphone|specialUse` declared; POST_NOTIFICATIONS granted
  via `EnsureNotificationPermissionEffect`.

## Real-mode architecture (added 2026-08)
- `AssistantMode` enum is the single source of truth for operating modes. `activeMode(settings)`
  resolves the active mode from persisted flags → a mode-specific system prompt that
  `AiModelRouter.buildSystemPrompt` composes on top of the base personality. Toggling a mode
  genuinely changes AI behaviour (Doctor/Female/Legal/Vehicle/Home/IT-Business/All-Rounder/Force),
  not just a UI badge.
- New mode flags added to `UserSettings` + Keys + flow loader + setters:
  `isFemaleModeEnabled`, `isLegalModeEnabled`, `isAllRounderModeEnabled`, `isHomeModeEnabled`,
  `isItBusinessModeEnabled`. UI toggles in `DoctorAndModesSettingsCard` (additive, UI preserved).
- `DoctorModeManager.generateDoctorResponse` (canned strings) is dead code — medical queries
  now route through the AI with the Doctor system prompt. Do NOT call the canned method.

## Security
- API keys (`userApiKey`) now stored via `SecureCredentialsStore` (AndroidKeyStore AES/GCM),
  NOT plain DataStore/Room. `updateAiProvider` writes only to the encrypted store and removes
  the legacy plain key. Load falls back to legacy DataStore value once for migration.
- `EncryptedLockCredentialsRepository` covers lock PIN/pattern/password the same way.

## Honesty fixes (no fake success)
- `AiRepository` no longer returns "Connected to AI Provider!" / "I received your message!" /
  "Claude API response received!" on empty/error responses — returns real error text instead.
- `SettingsApiScreen.performConnectionTest` now performs a real auth call and distinguishes
  2xx (valid key) vs 401/403 (bad key) vs network error — no longer lies "key ready" on a 401.
- `UniversalCommunicationManager` adapters now fire real Android intents (ACTION_DIAL,
  ACTION_SENDTO smsto/mailto, WhatsApp/Instagram ACTION_SEND) and report honest results
  ("dialer opened, tap call") instead of faking "message sent".
- `AssistantOrchestrator` smart-home branch toggles only a real configured device, else says
  no device is configured — never fakes "Smart Home action processed".
- `SmartSceneManager` unknown-scene branch no longer returns fake "executed successfully".

## Conventions
- UI is Compose; screens under `ui/screens/`, components under `ui/components/`, glass UI under `ui/glass/`.
- AGENTS.md skill is auto-loaded; keep this file updated with repo-specific gotchas.
