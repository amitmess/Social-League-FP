# Social League — Mobile Apps Final Project (Android)

Social League is a native Android application designed to manage private casual soccer leagues. The app features real-time database synchronization, Google Authentication, and geographical location tracking to guide players to match venues.

---

## Key Features & Screen Map

The application consists of exactly five functional screens:

1.  **Google Login (`LoginActivity`):**
    *   Secure gateway gating all application actions.
    *   Authenticates users using Google Sign-In and maps credentials to Firebase Authentication.
    *   Saves and caches session states for rapid auto-redirection on future app launches.
2.  **Matches List (`MatchesListActivity`):**
    *   Main dashboard divided dynamically into **Upcoming** and **Completed** sections.
    *   Utilizes a real-time Firestore listener; any score updates propagate across all online devices immediately.
    *   Provides quick toolbar entry points to user profile records.
3.  **Match Details (`MatchDetailsActivity`):**
    *   Displays full match metadata including datetime, teams, status, and attendee counts.
    *   Allows managers to submit final match scores with validation rules (must be non-negative integers).
    *   Directly persists edits back to Cloud Firestore, automatically moving the match to the completed section.
4.  **User Profile (`UserProfileActivity`):**
    *   Renders display name, email address, and avatar circular image retrieved directly from the cached `FirebaseUser` session.
    *   Manages session sign-out, clearing task flags, and redirecting users back to the Login gateway.
    *   Includes a hidden developer test crash callback (double tap/click on display name) to trigger a test exception for verifying Crashlytics logs.
5.  **Stadium Location (`StadiumLocationActivity`):**
    *   Performs device GPS telemetry checks. Handles location permissions (`ACCESS_FINE_LOCATION`) at runtime.
    *   Retrieves high-accuracy device lat/lng values using `FusedLocationProviderClient`.
    *   Computes geodesic distance in kilometers between the user and the match stadium using `Location.distanceBetween`.
    *   Launches turn-by-turn routing guidelines in Google Maps using an implicit navigation Intent.

---

## Tech Stack & Integrations

*   **Platform:** Native Android (Java), Compile/Target SDK 34, Min SDK 24.
*   **Database:** Cloud Firestore (real-time stream listeners for documents and collections).
*   **Authentication:** Firebase Authentication (Google OAuth provider).
*   **Diagnostics:** Firebase Crashlytics (captures location, database, and system level exceptions).
*   **Telemetry:** Firebase Analytics (tracks custom event logs for transitions, updates, and errors).
*   **Location:** Google Play Services Location API.
*   **Build System:** Gradle (Kotlin DSL), JDK 17.

---

## Setup & Configuration

### Prerequisites
1.  Add your `google-services.json` file inside the `app/` directory of the project.
2.  Ensure Google Sign-In is enabled in your Firebase console.
3.  Register your debug SHA-1 fingerprint under the Android application settings in the Firebase Console.

### Running the App
*   **Via Android Studio:** Import the root folder, let Gradle sync complete, and run the app on an Emulator (API 34 with Google Play services) or a connected debugging device.
*   **Via CLI:** If a device is connected, run:
    ```powershell
    .\gradlew installDebug
    ```

---

## Firestore Database Structure

*   **Collection: `matches`**
    *   `homeTeam`: String
    *   `awayTeam`: String
    *   `locationName`: String
    *   `dateTime`: String
    *   `status`: String (`"UPCOMING"` or `"PLAYED"`)
    *   `homeScore`: Integer (Nullable)
    *   `awayScore`: Integer (Nullable)
    *   `confirmed`: Integer (Attendance count)
    *   `maybe`: Integer (Attendance count)
    *   `latitude`: Double (Stadium coordinates)
    *   `longitude`: Double (Stadium coordinates)

*Note: The repository is configured to automatically seed 10 demo matches into Firestore if the collection is empty, ensuring sample listings are immediately visible.*
