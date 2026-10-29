# Umlindi — Neighborhood Safety Alert

Umlindi (isiZulu for "the watcher / guard") is an Android app that lets residents report and receive alerts about local safety incidents, such as break-ins, suspicious activity and load-shedding-related risks, and lets the community confirm or dispute each report.

Built in Kotlin as an IIE Portfolio of Evidence, following a Research → Plan → Design → Build → Evaluate cycle.

**Demo video:** [add unlisted YouTube link here]

**Version:** 2.0 (Final PoE)

## Screenshots

[add screenshots here: splash, login, register, home feed, report, alert detail, settings, isiZulu and Sesotho versions]

## Features

| Feature | Prototype (v1.0) | Final PoE (v2.0) |
|---|---|---|
| Email and password registration and login (hashed by Firebase Authentication) | Yes | Yes |
| Settings (display name, alert radius, language, home location) | Yes | Yes |
| REST API and database (Firebase Authentication and Cloud Firestore) | Yes | Yes |
| Real-time incident feed | Yes | Yes |
| Report an incident with GPS location | Yes | Yes |
| Single sign-on: Google | No | Yes |
| Single sign-on: Facebook | No | Yes |
| Offline mode with sync | No | Yes |
| Real-time notifications | No | Yes |
| Multi-language: English, isiZulu, Sesotho | No | Yes |
| Alert Detail screen | No | Yes |
| Community confirm / dispute voting | No | Yes |
| Trust score for reporters | No | Yes |
| Severity-aware alert radius | No | Yes |
| Forgot-password email | No | Yes |
| Final visual design, app icon and assets | No | Yes |

## Innovative features

1. **Severity-aware alert radius.** A report notifies a user only if it falls within that user's alert radius, and more serious incidents reach further: break-ins use twice the radius, suspicious activity uses the radius, and load-shedding risks and other incidents use half. This stops minor reports far away from flooding people with alerts.
2. **Community confirm / dispute voting.** Residents can confirm or dispute any report that is not their own. Each person has one vote per report, and they can change it.
3. **Trust score.** Each vote moves the reporter's trust score (+1 for a confirm, −1 for a dispute, ±2 when a vote is switched). The score is shown on the Alert Detail screen and in Settings, so false reports cost the reporter credibility without needing identity verification.

## Release notes

### v2.0: Final PoE

**Added**
- Google and Facebook sign-in alongside email and password. A first-time social user gets a profile document automatically.
- Offline mode: a report submitted without a connection is stored in a local Room database and uploaded automatically by WorkManager once a connection returns. A failed online submission is also queued the same way.
- In-app notifications for new reports from other users, driven by the Firestore real-time listener. Distance filtering uses the severity-aware radius described above.
- Multi-language support in English, isiZulu and Sesotho, with a language selector in Settings.
- Alert Detail screen with the reporter's name and trust score, confirm / dispute voting, and live vote counts.
- Trust score updates through a Firestore transaction, so a vote and the score change succeed or fail together.
- Forgot-password email from the login screen.
- Firestore security rules (see `firestore.rules`).
- New unit tests for distance calculation, alert rules and trust rules.

**Changed**
- Complete visual redesign to match the design document: teal theme, shield logo, redesigned splash, login, register, home, report, detail and settings screens.
- Validation logic moved into a shared `Validators` object used by every screen.
- Category labels, time text and severity text now come from string resources instead of hard-coded English.

**Fixed**
- Severity indicator colour no longer leaks between list rows.
- Location lookup now requests a fresh fix when no cached location exists.

## Design considerations

- **Firebase instead of a self-built server.** Authentication, the database and the real-time listeners come from one service. It keeps the project free to run and removes server code that would need hosting.
- **Offline-first reporting.** Reports are written locally first when there is no connection, because the situations where people most need to report an incident are often the ones with poor signal.
- **Notifications without Cloud Functions.** Server-sent push notifications need Cloud Functions, which require a paid billing plan. The app instead raises notifications from the Firestore real-time listener. The trade-off is that alerts arrive only while the app process is running.
- **Security.** Passwords are hashed by Firebase Authentication and never stored by the app. Firestore rules restrict reads to signed-in users, restrict writes to the owner of the data, and limit how far one user can change another user's trust score.
- **Testable logic.** Validation, formatting, distance, alert and trust rules live in plain Kotlin objects, so they are covered by fast unit tests that run on GitHub Actions without a device.

## Tech stack

- Kotlin, Android (minimum SDK 24), Jetpack Navigation (single activity, fragments)
- Firebase Authentication, Cloud Firestore
- Room and WorkManager for offline sync
- Credential Manager (Google) and the Facebook Login SDK
- Gradle (Kotlin DSL), Android Gradle Plugin 8.7.3, Kotlin 2.0.21, KSP
- GitHub Actions for continuous integration

## GitHub and GitHub Actions

The code is version controlled in this repository, with regular commits throughout development. The workflow in `.github/workflows/build.yml` runs on every push and pull request to `main`:

1. Checks out the code and sets up JDK 17.
2. Decodes `google-services.json` from an encrypted repository secret, since the file is deliberately not committed.
3. Runs the unit tests with `./gradlew test`.
4. Builds the debug APK and uploads it as a downloadable artifact.

The final commit is tagged `final-poe`.

## Project structure

```
app/src/main/java/com/project/umlindi/
├── MainActivity.kt
├── UmlindiApp.kt              # notification channel
├── data/local/                # Room database for the offline queue
├── sync/                      # WorkManager worker that uploads queued reports
├── util/                      # validators, formatters, distance, alert and trust rules
└── ui/
    ├── splash/   auth/   home/   detail/   report/   settings/
```

## Data model (Cloud Firestore)

- `users/{uid}`: `displayName`, `email`, `homeLocation` (GeoPoint), `alertRadiusKm`, `preferredLanguage`, `trustScore`, `createdAt`
- `reports/{reportId}`: `reporterId`, `category`, `description`, `location` (GeoPoint), `status`, `createdAt`, `updatedAt`
- `reports/{reportId}/votes/{voterUid}`: `voteType` (`confirm` or `dispute`), `createdAt`

## Setup

1. Clone the repository and open it in Android Studio.
2. **`google-services.json`** is not in the repository for security reasons. Either request the development file from [your email here], or create your own Firebase project with package name `com.project.umlindi`, enable Email/Password, Google and Facebook sign-in, create a Firestore database, publish `firestore.rules`, and place the downloaded file in `app/`.
3. **Facebook sign-in:** put your Facebook App ID and client token in `app/src/main/res/values/facebook.xml`, and register your debug key hash in the Facebook app settings.
4. **Google sign-in:** register your debug SHA-1 in the Firebase project settings.
5. Sync Gradle and run on a device or emulator with Google Play services.

## Testing

Unit tests cover `Validators`, `ReportFormatter`, `GeoUtils`, `AlertRules` and `TrustRules`. Run them with:

```
./gradlew testDebugUnitTest
```

## Known limitations and future work

- Notifications are raised by the running app. True push delivery when the app is closed needs a server (Cloud Functions or a small hosted service).
- The trust score is written by the client. A production version would calculate it on a server so it cannot be abused.
- The home screen is a list. A map view and photo attachments were designed but left out because they need paid services.
- Google sign-in and Facebook sign-in depend on the registered debug fingerprints. The signed release build needs its own SHA-1 and key hash added.
- The isiZulu and Sesotho translations should be reviewed by a first-language speaker.
