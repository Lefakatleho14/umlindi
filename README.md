# Umlindi — Neighborhood Safety Alert App

Umlindi (isiZulu for "the watcher/guard") is an Android app that lets residents report and receive alerts about local safety incidents — break-ins, suspicious activity, and load-shedding-related risks — in real time.

Built as part of an IIE academic Portfolio of Evidence (PoE), following a Research → Plan → Design → Build → Evaluate cycle.

## Features

- **Secure authentication** — Email/password registration and login via Firebase Authentication, with passwords hashed server-side (never stored or transmitted in plaintext by the app)
- **Single sign-on** — Google Sign-In integration
- **User settings** — Editable display name, alert radius, preferred language, and home location
- **Real-time incident feed** — Live-updating list of nearby reports via Cloud Firestore snapshot listeners
- **Incident reporting** — Submit reports with category, description, and GPS-based location (falls back to a fresh location request if no cached location is available)
- **Trust-score concept** (planned) — Community confirm/dispute voting to discourage false reports
- **Offline mode with sync** *(Final PoE)* — Local Room database queue for reports made without connectivity
- **Push notifications** *(Final PoE)* — Firebase Cloud Messaging alerts for nearby incidents
- **Multi-language support** *(Final PoE)* — English, isiZulu, and Sesotho

## Tech Stack

- **Language:** Kotlin
- **Architecture:** Fragment-based navigation using the Jetpack Navigation Component
- **Backend:** Firebase (Authentication, Cloud Firestore, planned: Cloud Messaging)
- **Local storage:** Room (for planned offline sync)
- **Build tooling:** Gradle (Kotlin DSL), Android Gradle Plugin 8.7.3, Kotlin 2.0.21, KSP for annotation processing
- **CI/CD:** GitHub Actions (automated build and test on push)

## Project Structure

```
app/src/main/java/com/project/umlindi/
├── MainActivity.kt
├── util/
│   ├── Validators.kt      # Input validation logic (unit tested)
│   └── ReportFormatter.kt # Category labels, severity colors, time formatting (unit tested)
└── ui/
    ├── splash/        # Splash screen, session check
    ├── auth/          # Login and Register screens
    ├── home/          # Alert feed (RecyclerView + Firestore listener)
    ├── report/        # Report Incident form
    └── settings/      # User settings screen
```

## Setup Instructions

1. Clone this repository:
   ```
   git clone https://github.com/Lefakatleho14/umlindi.git
   ```
2. Open the project in Android Studio.
3. **Firebase configuration file required:** This project needs a `google-services.json` file, which is **not included in this repository** for security reasons. To run the project:
   - Contact [your email/contact here] for the file used in development, **or**
   - Create your own Firebase project at [Firebase Console](https://console.firebase.google.com), register an Android app with package name `com.project.umlindi`, enable Email/Password and Google authentication, create a Firestore database, and download your own `google-services.json` into the `app/` directory.
4. Sync Gradle and run on an emulator or physical device (minimum SDK 24).

## Data Model (Firestore)

**`users` collection** — `userId`, `displayName`, `email`, `homeLocation` (GeoPoint), `alertRadiusKm`, `preferredLanguage`, `trustScore`, `createdAt`

**`reports` collection** — `reportId`, `reporterId`, `category`, `description`, `location` (GeoPoint), `status`, `createdAt`, `updatedAt`

## Testing

Unit tests cover core validation and formatting logic (see `app/src/test/`). Run locally with:
```
./gradlew test
```

## Demo Video

[Link to demo video — add once recorded]

## AI Tool Usage

This project was developed with assistance from Claude (Anthropic) for architecture guidance, Kotlin/Firebase code, Gradle troubleshooting, and documentation drafting. See the accompanying AI usage write-up for details.
