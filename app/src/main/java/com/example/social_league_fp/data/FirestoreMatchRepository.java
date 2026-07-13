package com.example.social_league_fp.data;

import android.util.Log;

import com.example.social_league_fp.model.Match;
import com.example.social_league_fp.model.MatchStatus;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreMatchRepository implements MatchRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION_MATCHES = "matches";
    private static final String TAG = "FirestoreRepository";

    @Override
    public ListenerRegistration getAllMatches(MatchesCallback callback) {
        return db.collection(COLLECTION_MATCHES)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        FirebaseCrashlytics.getInstance().recordException(error);
                        callback.onError(error);
                        return;
                    }

                    if (value != null && value.isEmpty()) {
                        seedDatabase();
                    }

                    List<Match> matches = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Match match = parseMatch(doc);
                            if (match != null) {
                                matches.add(match);
                            }
                        }
                    }
                    callback.onLoaded(matches);
                });
    }

    private void seedDatabase() {
        String[] homeTeams = {"Maccabi", "Hapoel", "Beitar", "Maccabi Haifa", "Hapoel Beer Sheva", "Bnei Sakhnin", "Maccabi Tel Aviv", "Hapoel Haifa", "Ashdod", "Netanya"};
        String[] awayTeams = {"Hapoel Tel Aviv", "Maccabi Petah Tikva", "Hapoel Jerusalem", "Bnei Yehuda", "Hapoel Hadera", "Ironi Kiryat Shmona", "Maccabi Jaffa", "Hapoel Ramat Gan", "Hapoel Umm al-Fahm", "Maccabi Herzliya"};
        String[] locations = {"Tel Aviv Stadium", "Haifa Arena", "Jerusalem Park", "Beer Sheva Field", "Netanya Stadium", "Petah Tikva Field", "Ashdod Court", "Sakhnin Field", "Hadera Park", "Herzliya Arena"};

        for (int i = 0; i < 10; i++) {
            Map<String, Object> match = new HashMap<>();
            match.put("homeTeam", homeTeams[i]);
            match.put("awayTeam", awayTeams[i]);
            match.put("location", locations[i]);
            match.put("dateTime", "Feb " + (10 + i) + ", 2026 - 20:00");
            match.put("confirmed", (int) (Math.random() * 15) + 5);
            match.put("maybe", (int) (Math.random() * 5));
            match.put("latitude", 32.0853 + (i * 0.01));
            match.put("longitude", 34.7818 + (i * 0.01));

            if (i < 4) { // 4 PLAYED
                match.put("status", "PLAYED");
                match.put("homeScore", (int) (Math.random() * 5));
                match.put("awayScore", (int) (Math.random() * 5));
            } else { // 6 UPCOMING
                match.put("status", "UPCOMING");
                match.put("homeScore", null);
                match.put("awayScore", null);
            }

            db.collection(COLLECTION_MATCHES)
                    .add(match)
                    .addOnSuccessListener(documentReference ->
                            Log.d(TAG, "Seeded match: " + documentReference.getId()))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to seed database", e);
                        FirebaseCrashlytics.getInstance().recordException(e);
                    });
        }
    }

    @Override
    public ListenerRegistration getMatchById(String id, MatchCallback callback) {
        return db.collection(COLLECTION_MATCHES).document(id)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        FirebaseCrashlytics.getInstance().recordException(error);
                        callback.onError(error);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Match match = parseMatch(snapshot);
                        callback.onLoaded(match);
                    } else {
                        callback.onError(new Exception("Match not found"));
                    }
                });
    }

    @Override
    public void updateScore(String id, int homeScore, int awayScore, Runnable onSuccess, OnError onError) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("homeScore", homeScore);
        updates.put("awayScore", awayScore);
        updates.put("status", "PLAYED");

        db.collection(COLLECTION_MATCHES).document(id)
                .update(updates)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    onError.onError(e);
                });
    }

    private Match parseMatch(DocumentSnapshot doc) {
        try {
            String statusText = doc.getString("status");
            MatchStatus status = "PLAYED".equals(statusText)
                    ? MatchStatus.PLAYED
                    : MatchStatus.UPCOMING;

            Double latitude = doc.getDouble("latitude");
            Double longitude = doc.getDouble("longitude");

            return new Match(
                    doc.getId(),
                    doc.getString("homeTeam"),
                    doc.getString("awayTeam"),
                    doc.getString("dateTime"),
                    doc.getString("location"),
                    status,
                    doc.getLong("homeScore") == null ? null : doc.getLong("homeScore").intValue(),
                    doc.getLong("awayScore") == null ? null : doc.getLong("awayScore").intValue(),
                    doc.getLong("confirmed") == null ? 0 : doc.getLong("confirmed").intValue(),
                    doc.getLong("maybe") == null ? 0 : doc.getLong("maybe").intValue(),
                    latitude,
                    longitude
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse document: " + doc.getId(), e);
            FirebaseCrashlytics.getInstance().recordException(e);
            return null;
        }
    }
}
