# Habits

Your apps stay locked until today's habits are done.

Pick the apps that eat your day (Instagram, TikTok, …) and the habits you want to do every day.
Blocked apps are **locked each day until every habit is done**, then open until midnight.

- **Timed habits** have a daily goal (e.g. *Read 30 min*); time adds up across sessions. Use a
  plain timer or Pomodoro (25 min focus / 5 min break, only focus counts).
- **Check-off habits** are done with one tap (e.g. *Make bed*).
- Open a locked app and you get a lock screen showing what's left.
- Home-screen widget: long-press your home screen → Widgets → Habits.

## Install

1. On your phone, download `habits.apk` from this repo's `latest` release (rebuilt on every push)
   and open it. Allow installing from your browser when asked.
2. In the app's **Setup** tab:
   - **Blocker** (required): turn on the Habits accessibility service. On Android 13+ it's greyed
     out for sideloaded apps — go to *Settings → Apps → Habits → ⋮ → Allow restricted settings* first.
   - **Notifications**: for the timer and Pomodoro alerts.
   - **Battery**: "don't optimize", so the phone doesn't kill the blocker. On Samsung/Xiaomi/OnePlus
     also set *App info → Battery → Unrestricted*.
3. **Apps** tab → tick the apps to lock. **Habits** tab → add your habits.

To update, install the newer APK over the old one; your data is kept.

## Limitations

- You can always turn the accessibility service off or uninstall the app. Android doesn't let a
  normal app prevent that.
- Timers and check-offs are on the honor system.

## Building

Kotlin + Jetpack Compose + Room. CI (`.github/workflows/build.yml`) runs the unit tests and builds
the APK on every push; locally, `./gradlew assembleRelease` with an Android SDK. The APK is signed
with `app/debug.keystore` so each build installs as an update of the last.

Font: [Inter](https://rsms.me/inter/), under the SIL Open Font License.
