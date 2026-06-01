# Social League — Mobile Apps Final Project (Android)

## Overview
Social League is a simple Android app prototype for managing a private sports league.
This submission implements **2 key screens** with **dynamic data (RecyclerView)** and a core action (**Submit Score**) as required.

✅ Offline only (no API)  
✅ No persistence (no DB / files) — in-memory data only

---

## Key Features
### 1) Matches List (RecyclerView)
- Displays matches split into **Upcoming** and **Completed**
- Each match is shown as a styled card (dark UI)
- Clicking a match opens the details screen

### 2) Match Details
- Shows match details (teams, date/time, location, status)
- **Submit Score**: enter home/away score → match becomes **Completed** and score updates in the list
- Top-left back arrow returns to the list

---

## Tech Stack / Tools
- Android (Java)
- XML layouts (Material UI styling)
- RecyclerView (dynamic lists)
- InMemory Repository (data stored in RAM)
- GitHub for version control

---

## Project Structure (Main)
- `ui/matches/MatchesListActivity` — list screen (Upcoming/Completed RecyclerViews)
- `ui/matches/MatchesAdapter` — binds match cards
- `ui/details/MatchDetailsActivity` — details + Submit Score
- `data/InMemoryMatchRepository` — in-memory data source
- `model/Match`, `model/MatchStatus` — data models

---

## How to Run
### Option A: Android Studio (recommended)
1. Clone the repo:
   git clone <YOUR_GITHUB_REPO_URL>
2. Open the project folder in Android Studio
3. Let Gradle sync finish
4. Run the app on an Emulator (e.g., Pixel 6) or a physical device

### Option B: IntelliJ IDEA (Android plugin + SDK required)
1. Open the project as a Gradle project
2. Ensure:
  - Gradle JVM is set to JDK 17
  - Android SDK platform (compileSdk) is installed
3. Run on emulator/device

---

## Usage Flow (Quick Test)
1. Open the app → Matches List appears
2. Tap an upcoming match → opens Match Details
3. Enter Home/Away score → Save
4. Return to Matches List → match moves to Completed and displays the score

---

## Notes / Constraints
- No backend, no network calls
- No database or file storage — data resets on app restart (in-memory only)

---

## Team
Amit Messil,
Dan Madpis,
Ran Efroni,
Amit Mane
