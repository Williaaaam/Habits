# Habits

An Android app that puts your habits before your apps, modeled on
[Habits First](https://habitsfirst.com/) for iPhone. Pick the apps that eat your day (Instagram,
TikTok, …) and the habits you want to do every day. The apps stay **locked each day until all of
today's habits are done**, then unlock until midnight.

## How it works

- **Timed habits** have a daily goal (e.g. *Read 30 min*). Time adds up across sessions. Start a
  plain timer or a **Pomodoro** timer (25 min focus / 5 min break, only focus time counts, with an
  alert at each switch).
- **Check-off habits** are done with one tap (e.g. *Make bed*).
- Open a blocked app before you're done and a lock screen shows what's left, with buttons to start
  a timer or check habits off. Once the last habit is done it offers to open the app.
- **Progress** tab: a GitHub-style heatmap of your days, current and best streak, plus a heatmap
  and streak per habit.
- **Home-screen widget**: today's habits and whether your apps are locked.
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
   - Pomodoro alerts use exact alarms. That's allowed automatically on most phones.
4. **Apps** tab → tick Instagram etc. **Habits** tab → add "Read, 30 min" and so on.
5. Optional: long-press your home screen → Widgets → Habits to add the widget.

Updating: download the newer `habits.apk` and install it over the old one; your data is kept.
(Exception: the first build used a "minutes for minutes" system; its data is reset once on upgrade.)

## Limitations

- You can always switch the accessibility service off or uninstall the app. Android doesn't let a
  normal app prevent that — the friction is the point.
- Habit timers and check-offs are on the honor system: the app knows the timer is running, not
  that you're reading.
- Not included (yet) from Habits First: auto-tracked habits (steps/workouts via Health Connect),
  commitment locks, and schedule/location/Bluetooth-based blocks.

## Building

Built with Kotlin + Jetpack Compose + Room. CI (`.github/workflows/build.yml`) runs the unit tests
and builds the APK on every push. Locally: open the project in Android Studio, or run
`./gradlew assembleRelease` with an Android SDK installed. The APK is signed with the key in
`app/debug.keystore` so every build can be installed as an update of the previous one.
