# MoneyMate

An Android personal-finance app that helps you keep track of your money: accounts, transactions,
monthly budgets and savings goals — with a secure sign-in and a cloud copy of your data.

Built with **Kotlin** and **Jetpack Compose** in Android Studio.

> **Demo prototype.** All balances and transactions are simulated sample data in rand (ZAR).
> No real bank account is connected — real bank linking would require a provider account
> (for example Stitch) before it could be switched on.

---

## Features

- **Register & sign in** — email and password with a 10-character minimum, handled by Firebase Authentication
- **Dashboard** — total balance, per-account balances, income and spending for the month
- **Transactions** — add money in / money out, categorise, delete; the account balance adjusts automatically
- **Budgets** — a monthly limit per category with a live progress bar that turns red when you overspend
- **Goals** — savings targets with progress, contribute money at any time
- **Settings** — dark mode, hide all balances, change password, reset the demo data
- **Offline first** — the phone keeps its own database, so the app works in aeroplane mode and syncs when you are back online

## Screens

| Splash | Home | Add transaction | Budgets | Goals | Settings |
|---|---|---|---|---|---|
| ![splash](docs/screenshots/splash.png) | ![home](docs/screenshots/home.png) | ![add](docs/screenshots/add.png) | ![budgets](docs/screenshots/budgets.png) | ![goals](docs/screenshots/goals.png) | ![settings](docs/screenshots/settings.png) |

*(Add your own screenshots to `docs/screenshots/` and the table above will show them.)*

## How it works

```
                ┌──────────────────────────────┐
                │   Compose screens (UI)       │
                │   MainActivity.kt            │
                └───────────────┬──────────────┘
                                │  Api.get / Api.post  (same paths as the old REST API)
                ┌───────────────▼──────────────┐
                │   MmRepo.kt                  │
                │   sign-in, sync, dashboard   │
                └───────┬──────────────┬───────┘
                        │              │
        ┌───────────────▼───┐   ┌──────▼────────────────────┐
        │ Room (MmDatabase) │   │ Firestore (cloud copy)    │
        │ on the phone      │◄──┤ users/{uid}/...           │
        └───────────────────┘   └───────────────────────────┘
                        ▲
                        │
        ┌───────────────┴──────────────┐
        │ Firebase Authentication      │
        │ email + hashed password      │
        └──────────────────────────────┘
```

- **Firebase Authentication** — says *who you are*. Passwords are hashed and stored by Firebase, never by the app.
- **Room** — the database on the phone. Every read and write goes here first, so the app is instant and works offline.
- **Firestore** — a cloud copy of the same data. Rows changed while offline are marked and pushed up as soon as there is internet. Deletions made offline are remembered so the cloud copy is cleaned up instead of resurrecting them.

## Tech stack

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Sign-in | Firebase Authentication (Email/Password) |
| On-device database | Room (SQLite) |
| Cloud database | Cloud Firestore |
| Money handling | Integer cents, displayed as ZAR (`R 48 650.00`) |
| Build | Gradle, Android Gradle Plugin 9.x, Kotlin 2.2 |

## Project structure

```
app/src/main/java/com/example/moneymate/
├── MainActivity.kt    # every screen: splash, auth, home, budgets, goals, activity, settings
├── MmDatabase.kt      # Room entities (tables), DAO and the database
└── MmRepo.kt          # Firebase auth + Room read/write + Firestore sync + dashboard JSON
app/src/main/res/      # launcher icon, colours, strings, network security config
Firestore rules        # see below
```

## Getting started

1. Clone the repository and open the `MoneyMate` folder in Android Studio.
2. Add your own `google-services.json` (Firebase console → Project settings → your Android app)
   directly inside the `app/` folder.
3. In the Firebase console, switch on **Authentication → Email/Password** and create a
   **Firestore** database.
4. Paste these Firestore rules and press **Publish**:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

5. Sync Gradle, then press **Run**.

## Sample data every new account starts with

| | |
|---|---|
| Accounts | Everyday account •4092 (R 26 150.00), Pocket savings •8016 (R 22 500.00) |
| Total balance | **R 48 650.00** |
| Transactions | Salary +R 32 500.00, Woolworths −R 842.30, Vodacom −R 399.00, Uber −R 116.00, Checkers −R 518.75 |
| Budgets | Groceries R 4 000.00 · Transport R 1 500.00 · Bills R 2 500.00 |
| Goals | Emergency fund R 8 500 / R 30 000 · December holiday R 3 000 / R 12 000 |

## Limitations / not built yet

- No real bank account is linked (needs a provider account and credentials)
- No forgot-password email flow (needs an email service)
- If two devices edit the same record offline, the last one to sync wins (no merge logic)
- Sign-in needs internet once; after that the app runs entirely from Room

## Author

Nhlayiseko Ndlala
ST10448654 — 

here is the link to my video

https://advtechonline-my.sharepoint.com/:v:/r/personal/st10442038_rcconnect_edu_za/Documents/video-output-CB627B1A-AB12-445C-8DC8-5333CF0135B5-1.mov?d=w3e570e800c36469495dd8292591ea36c&csf=1&web=1&nav=eyJyZWZlcnJhbEluZm8iOnsicmVmZXJyYWxBcHAiOiJPbmVEcml2ZUZvckJ1c2luZXNzIiwicmVmZXJyYWxBcHBQbGF0Zm9ybSI6IldlYiIsInJlZmVycmFsTW9kZSI6InZpZXciLCJyZWZlcnJhbFZpZXciOiJNeUZpbGVzTGlua0NvcHkifX0&e=zrrpDG

