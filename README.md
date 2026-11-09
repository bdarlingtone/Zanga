# ZANGA – Premium Personal Life Management & Productivity Assistant

> **Official Tagline:** *Take Control. Own Your Time.*

---

## Overview

**ZANGA** is an intelligent Personal Life Management and Productivity Ecosystem designed to help individuals take control of their time, manage finances, monitor responsibilities, build habits, achieve personal goals, and maintain rigorous discipline.

Unlike conventional to-do apps, ZANGA functions as an end-to-end command center featuring an **Intelligent Alarm and Countdown Engine**, financial ledgers in **MWK (Malawi Kwacha)**, 24-hour daily timeline visualization, full-screen **Focus Mode**, habit streak tracking, and an **AI Personal Assistant**.

---

## 🎨 Visual Identity & Brand System

* **Brand Name:** ZANGA
* **Palette:**
  * **Primary Blue:** `#1769D2`
  * **Deep Blue:** `#071A36`
  * **Pure Black:** `#080A0F`
  * **White:** `#FFFFFF`
  * **Light Background:** `#F5F7FA`
  * **Secondary Blue:** `#3B82F6`
* **Iconography:** Custom luxury chronometer time dial with stylized 'Z' precision glyph.
* **Themes:** Full dynamic Dark theme (Pure Black & Deep Blue) and Light theme.

---

## 🚀 Key Modules & Capabilities

1. **Main Executive Dashboard**
   - Real-time digital clock with seconds, date, and personalized greeting (*"Good Morning, Commander"*).
   - Live **Active Countdown Card** tracking the active or upcoming activity with instant **Focus** and **Done** actions.
   - **Discipline Score Gauge:** Calculated rating (0–100) reflecting punctuality, consistency, and budget control.
   - Schedule conflict detection alerts with alternate slot recommendations.
   - Daily financial budget overview and quick-action command tiles.

2. **Intelligent Alarm & Countdown Engine (Signature Feature)**
   - Exact alarms scheduled via native Android `AlarmManager`.
   - **Progressive Checkpoints:**
     - **30 min remaining:** Initial warning reminder.
     - **15 min remaining:** Second countdown warning.
     - **5 min remaining:** Urgent countdown alert.
     - **0 min remaining (Deadline):** High-priority alert:
       > *"TIME IS UP! Your allocated time for [Activity] has expired."*
   - Interactive heads-up notification and in-app alert modal:
     - *Mark as Completed*
     - *Extend Time (+15 Min, +30 Min)*
     - *Reschedule*
     - *Stop Alarm*
   - Automatic reboot alarm recovery via `BootReceiver`.

3. **Financial Command Center (Default: MWK)**
   - **Income & Expense Tracking:** Log income sources, expense categories, amounts in Malawi Kwacha (MWK), payment methods, and notes.
   - **Budgeting Engine:** Daily, weekly, and monthly spending limits with overspending warnings.
   - **Obligations & Debts:** Track debts owed to others and money lent to others with due dates and settlement states.
   - **Savings Targets:** Set target reserves with deadlines, monitor percentage progress rings, and log incremental deposits.

4. **24-Hour Daily Timeline & Schedule Visualization**
   - Chronological visual schedule from 06:00 to 23:00.
   - Automatic detection of available free time slots with one-tap quick scheduling.
   - Schedule collision warning banner suggesting optimal alternative time windows.

5. **Dedicated Focus Mode**
   - Full-screen immersion in Pure Black and Deep Blue.
   - Large live circular sweep countdown timer with remaining time in `MM:SS`.
   - Real-time elapsed time tracking, pause/resume controls, and `+15 Min` quick extensions.

6. **Academic & Professional Development**
   - Class timetables, study schedules (e.g. *Study C++ Programming*), assignment tracking, exam dates, and skill practice sessions.

7. **Routines & Habit Mastery**
   - Recurring daily habits (e.g. *Morning Deep Work*, *Reading 25 Pages*, *Physical Conditioning*).
   - Streak counters (Current Streak & Best Streak) and daily completion check-offs.

8. **Meetings & Appointments**
   - Executive calendar with venue, date, time intervals, attendees, agendas, and advance reminders.

9. **AI Personal Assistant**
   - Natural language comprehension supporting commands like:
     - *"Remind me to study C++ tomorrow from 8 AM to 10 AM"*
     - *"Record an expense of K15,000 for transport"*
     - *"Schedule a meeting with my team on Friday at 2 PM"*
     - *"How much money did I spend this month?"*
     - *"Show me all my incomplete activities"*
     - *"Help me organise my day"*
   - Interprets intents, extracts dates, times, and amounts, and provides one-tap confirmation action cards. Operates offline-first.

10. **Discipline Score & Analytics**
    - Multi-factor productivity formula evaluating on-time punctuality (40 pts), habit streaks (25 pts), budget control (15 pts), and overdue penalties.
    - Category-based time utilization distribution.
    - Exportable executive reports (Daily, Weekly, Monthly) to CSV and clipboard.

11. **Settings, Security & Offline Backup**
    - Full offline SQLite Room database with JSON data export and restoration.
    - Configurable currency, alarm chimes, haptic vibration, and default activity duration.

---

## 🛠️ Technical Stack

* **Language:** Kotlin (100%)
* **UI Toolkit:** Jetpack Compose & Material 3 (M3)
* **Architecture:** MVVM + Clean Architecture with Coroutines & StateFlow
* **Database:** SQLite via Room Database (Offline-first)
* **Scheduling:** Android `AlarmManager`, `NotificationManager`, and Broadcast Receivers
* **Platform:** Android 7.0+ (API 24 to API 36)

---

## 📦 Building and Downloading the APK

### Fast Build via Gradle
```bash
gradle assembleDebug
```
The output APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Exporting via Google AI Studio
1. **Push to GitHub:** Click the **GitHub icon / Export** button in the top right menu of AI Studio to link your GitHub account and create a new repository directly.
2. **Download APK / ZIP:** Open the **Project Settings** or **Export** menu in the top toolbar to download the complete project as a ZIP archive or retrieve the compiled APK.
