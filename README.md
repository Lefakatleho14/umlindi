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

## Screenshots

<img width="317" height="475" alt="image" src="https://github.com/user-attachments/assets/84feb44e-aeab-4619-9ca7-28deb67a46de" />
<img width="284" height="440" alt="image" src="https://github.com/user-attachments/assets/5dff8a94-17ba-4f8f-91e1-d5d1451d4e5e" />
<img width="314" height="469" alt="image" src="https://github.com/user-attachments/assets/dd6e20d5-e2e0-47e7-aca1-9c884afdff86" />
<img width="272" height="463" alt="image" src="https://github.com/user-attachments/assets/0cd8e3a5-576a-4058-8492-eedde06c5cff" />
<img width="238" height="418" alt="image" src="https://github.com/user-attachments/assets/c0d26d5a-2e9f-468c-9b1b-7bb3b289bbe4" />
<img width="210" height="367" alt="image" src="https://github.com/user-attachments/assets/38894f9f-a717-4c6b-85d3-727a3fe65c9f" />

**Play Store listing text:**
Umlindi: Google Play listing draft (v2.0)
Store listing
App name (30 characters max):
Umlindi: Neighborhood Safety
Short description (80 characters max):
Report incidents and get alerts about safety in your neighborhood.
Full description (4000 characters max):
Umlindi (isiZulu for "the watcher") helps you and your neighbours look out for each other.
Report what you see. Break-ins, suspicious activity and load-shedding risks can be reported in a few taps, with your location attached. No signal? Your report is saved on your phone and sent automatically once you are back online.
Get alerts that matter. Set your home location and alert radius, and Umlindi notifies you about new reports near you. More serious incidents reach further, so a break-in nearby is never lost among minor reports.
Let the community decide what is real. Open any alert to see who reported it, confirm it or dispute it. Every vote adjusts the reporter's trust score, which makes false reports cost credibility.
Features
Report incidents with category, description and GPS location
Live feed of recent alerts
Notifications for nearby reports, with a severity-aware radius
Confirm or dispute reports, with a trust score for reporters
Works offline: reports sync when you reconnect
Sign in with email, Google or Facebook
Available in English, isiZulu and Sesotho
Umlindi is a community tool and does not replace the police or emergency services. In an emergency, contact them directly.
Category: Lifestyle (alternative: Tools)
Contact email: [add your email]
Privacy policy URL: [required before publishing, see "Gaps to know about"]
Graphics
Asset	File	Requirement
App icon	umlindi-icon-512.png	512 x 512 PNG
Feature graphic	umlindi-feature-graphic-1024x500.png	1024 x 500 PNG
Phone screenshots	take on the phone	at least 2, up to 8
Screenshot checklist (take these on the phone)
Splash screen
Login screen with the Google and Facebook buttons
Register screen
Home feed with several reports
Report incident form, filled in
Alert Detail with confirm / dispute and the trust score
Settings in English
Any one screen in isiZulu or Sesotho
Tips: use a phone with a normal 16:9 or taller screen, take them in portrait, and use reports with realistic, harmless text.
What's new (release notes, 500 characters max)
Version 2.0: sign in with Google or Facebook, report incidents offline and sync later, alerts for nearby reports, confirm or dispute reports with a trust score, and a new design. Now available in English, isiZulu and Sesotho.
Data safety form (answers to prepare)
Data	Collected	Why
Name (display name)	Yes	Account and showing who reported
Email address	Yes	Account and login
Precise location	Yes	Attaching reports and alert distance
User IDs	Yes	Account management
Other user-generated content (reports, votes)	Yes	App functionality
Data is encrypted in transit (Firebase uses HTTPS).
Data is processed by Firebase (Google) as a service provider.
Passwords are hashed by Firebase Authentication and never stored by the app.
Content rating and audience
Complete the content rating questionnaire honestly: the app contains user-generated content and shares user location with a backend. Target audience: adults (18+) is the safest choice for an app that deals with crime reports.
Release file
Signed APK: `app-release.apk`, signed with `umlindi-release.jks` (kept outside the repository). Fingerprints registered with Firebase and Facebook.
Gaps to know about
These are honest limits of the current build, and they are worth stating in your write-up:
Privacy policy. Play requires a public privacy policy URL for apps that handle location and personal data. A short policy page can be hosted for free on GitHub Pages.
Account deletion. Play requires apps that create accounts to offer a way to delete the account and its data, both in the app and through a web link. The app does not have this yet, so it would be the first thing to build before publishing.
Upload not done. Registering a Play Console developer account has a one-time fee, so the listing is prepared but not uploaded.
**App icon (512×512):**
<img width="512" height="512" alt="umlindi-icon-512" src="https://github.com/user-attachments/assets/741e3046-fa5d-48b2-ab5a-6f2a196f1e0c" />

**Feature graphic (1024×500):**
<img width="1024" height="500" alt="umlindi-feature-graphic-1024x500" src="https://github.com/user-attachments/assets/08f2f6b3-bffa-43f5-a123-3d76e6e81a02" />

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
## Video link
Youtube: https://youtu.be/jh4vaomQMEQ 

## References

Android Developers (n.d.a) Schedule persistent work with WorkManager. Available at: https://developer.android.com/topic/libraries/architecture/workmanager (Accessed: 20 October 2026).

Android Developers (n.d.b) Save data in a local database using Room. Available at: https://developer.android.com/training/data-storage/room (Accessed: 20 October 2026).

Android Developers (n.d.c) Notifications overview. Available at: https://developer.android.com/develop/ui/views/notifications (Accessed: 20 October 2026).

Android Developers (n.d.d) Navigation. Available at: https://developer.android.com/guide/navigation (Accessed: 20 October 2026).

Android Developers (n.d.e) ConnectivityManager. Available at: https://developer.android.com/reference/android/net/ConnectivityManager (Accessed: 20 October 2026).

Sinnott, R.W. (1984) 'Virtues of the haversine', Sky and Telescope, 68(2), p. 159.
