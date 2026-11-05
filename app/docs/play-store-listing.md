# Umlindi: Google Play listing draft (v2.0)

## Store listing

**App name (30 characters max):**
Umlindi: Neighborhood Safety

**Short description (80 characters max):**
Report incidents and get alerts about safety in your neighborhood.

**Full description (4000 characters max):**

Umlindi (isiZulu for "the watcher") helps you and your neighbours look out for each other.

Report what you see. Break-ins, suspicious activity and load-shedding risks can be reported in a few taps, with your location attached. No signal? Your report is saved on your phone and sent automatically once you are back online.

Get alerts that matter. Set your home location and alert radius, and Umlindi notifies you about new reports near you. More serious incidents reach further, so a break-in nearby is never lost among minor reports.

Let the community decide what is real. Open any alert to see who reported it, confirm it or dispute it. Every vote adjusts the reporter's trust score, which makes false reports cost credibility.

Features
- Report incidents with category, description and GPS location
- Live feed of recent alerts
- Notifications for nearby reports, with a severity-aware radius
- Confirm or dispute reports, with a trust score for reporters
- Works offline: reports sync when you reconnect
- Sign in with email, Google or Facebook
- Available in English, isiZulu and Sesotho

Umlindi is a community tool and does not replace the police or emergency services. In an emergency, contact them directly.

**Category:** Lifestyle (alternative: Tools)

**Contact email:** [add your email]

**Privacy policy URL:** [required before publishing, see "Gaps to know about"]

## Graphics

| Asset | File | Requirement |
|---|---|---|
| App icon | umlindi-icon-512.png | 512 x 512 PNG |
| Feature graphic | umlindi-feature-graphic-1024x500.png | 1024 x 500 PNG |
| Phone screenshots | take on the phone | at least 2, up to 8 |

## Screenshot checklist (take these on the phone)

1. Splash screen
2. Login screen with the Google and Facebook buttons
3. Register screen
4. Home feed with several reports
5. Report incident form, filled in
6. Alert Detail with confirm / dispute and the trust score
7. Settings in English
8. Any one screen in isiZulu or Sesotho

Tips: use a phone with a normal 16:9 or taller screen, take them in portrait, and use reports with realistic, harmless text.

## What's new (release notes, 500 characters max)

Version 2.0: sign in with Google or Facebook, report incidents offline and sync later, alerts for nearby reports, confirm or dispute reports with a trust score, and a new design. Now available in English, isiZulu and Sesotho.

## Data safety form (answers to prepare)

| Data | Collected | Why |
|---|---|---|
| Name (display name) | Yes | Account and showing who reported |
| Email address | Yes | Account and login |
| Precise location | Yes | Attaching reports and alert distance |
| User IDs | Yes | Account management |
| Other user-generated content (reports, votes) | Yes | App functionality |

- Data is encrypted in transit (Firebase uses HTTPS).
- Data is processed by Firebase (Google) as a service provider.
- Passwords are hashed by Firebase Authentication and never stored by the app.

## Content rating and audience

Complete the content rating questionnaire honestly: the app contains user-generated content and shares user location with a backend. Target audience: adults (18+) is the safest choice for an app that deals with crime reports.

## Release file

Signed APK: `app-release.apk`, signed with `umlindi-release.jks` (kept outside the repository). Fingerprints registered with Firebase and Facebook.

## Gaps to know about

These are honest limits of the current build, and they are worth stating in your write-up:

- **Privacy policy.** Play requires a public privacy policy URL for apps that handle location and personal data. A short policy page can be hosted for free on GitHub Pages.
- **Account deletion.** Play requires apps that create accounts to offer a way to delete the account and its data, both in the app and through a web link. The app does not have this yet, so it would be the first thing to build before publishing.
- **Upload not done.** Registering a Play Console developer account has a one-time fee, so the listing is prepared but not uploaded.
