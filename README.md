# Habits

An Android app that makes you earn your screen time. Pick the apps that eat your day (Instagram,
TikTok, …) and the habits you want to build (reading, working out, …). Blocked apps stay locked
until you've done a habit: e.g. **30 min of reading → 15 min of Instagram**.

## How it works

- **Habits** have an exchange rate: do it for X minutes, earn Y minutes of app time. Partial time
  counts proportionally (10 min of reading at 30→15 earns 5 min), rounded down to whole minutes.
- To do a habit, tap **Start** in the app. While the timer runs, all blocked apps are locked.
  Tap **Stop & collect** (or *Stop* in the notification) to bank the earned time.
- Opening a blocked app uses up your credit second by second while it's on screen. When the credit
  runs out, a lock screen appears with buttons to start a habit.
- Credit is one pool shared by all blocked apps, and it **resets at midnight**.
- There is no emergency unlock.
- The **Today** tab also shows how long you've spent in each blocked app today.

## Install

1. On your phone, open the **Releases** page of this repo and download `habits.apk` from the
   `latest` release (it's rebuilt automatically on every push).
2. Open the file. Android will ask you to allow installing from your browser/files app — allow it.
3. Open **Habits** → **Setup** tab and go through the steps:
   - **Accessibility service** (required — this is what locks apps).
     On Android 13+, a sideloaded app's accessibility switch is greyed out ("Restricted setting").
     Fix: *Settings → Apps → Habits → ⋮ (top right) → Allow restricted settings*, then turn it on.
   - **Usage access** — for the screen-time numbers.
   - **Notifications** — for the timer notification.
   - **Battery: don't optimize** — otherwise some phones (Samsung, Xiaomi, OnePlus…) kill the
     blocker in the background. Also set *App info → Battery → Unrestricted* if your phone has it.
4. **Blocked apps** tab → tick Instagram etc. **Habits** tab → add "Reading, 30 → 15".

Updating: download the newer `habits.apk` and install it over the old one; your data is kept.

## Limitations

- You can always switch the accessibility service off or uninstall the app. Android doesn't let a
  normal app prevent that — the friction is the point.
- Habit timers are on the honor system: the app knows the timer is running, not that you're reading.

## Building

Built with Kotlin + Jetpack Compose + Room. CI (`.github/workflows/build.yml`) runs the unit tests
and builds the APK on every push. Locally: open the project in Android Studio, or run
`./gradlew assembleRelease` with an Android SDK installed. The APK is signed with the key in
`app/debug.keystore` so every build can be installed as an update of the previous one.
