package com.example.social_league_fp.ui.matches;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_league_fp.R;
import com.example.social_league_fp.data.FirestoreMatchRepository;
import com.example.social_league_fp.data.MatchRepository;
import com.example.social_league_fp.model.Match;
import com.example.social_league_fp.model.MatchStatus;
import com.example.social_league_fp.ui.details.MatchDetailsActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.auth.FirebaseAuth;
import com.example.social_league_fp.ui.auth.LoginActivity;
import com.example.social_league_fp.ui.profile.UserProfileActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Main matches dashboard displaying list divisions of upcoming and completed matches.
 * Gated by Firebase session status and synced in real-time with Firestore.
 */
public class MatchesListActivity extends AppCompatActivity {

    private MatchesAdapter upcomingAdapter;
    private MatchesAdapter completedAdapter;
    private FirebaseAnalytics firebaseAnalytics;
    private MatchRepository repository;
    private ListenerRegistration matchesListener;

    private ProgressBar progressBar;
    private TextView tvNoUpcoming;
    private TextView tvNoCompleted;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check: Redirect unauthenticated app launches to the gatekeeper Login screen.
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_matches_list);

        repository = new FirestoreMatchRepository();
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Log dashboard access metrics to Firebase Analytics.
        Bundle bundle = new Bundle();
        bundle.putString("screen_name", "matches_list");
        firebaseAnalytics.logEvent("open_matches_screen", bundle);

        progressBar = findViewById(R.id.progressBar);
        tvNoUpcoming = findViewById(R.id.tvNoUpcoming);
        tvNoCompleted = findViewById(R.id.tvNoCompleted);

        // Navigate to user profile settings screen on click.
        View btnProfile = findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(MatchesListActivity.this, UserProfileActivity.class);
                startActivity(intent);
            });
        }

        RecyclerView rvUpcoming = findViewById(R.id.rvUpcoming);
        RecyclerView rvCompleted = findViewById(R.id.rvCompleted);

        rvUpcoming.setLayoutManager(new LinearLayoutManager(this));
        rvCompleted.setLayoutManager(new LinearLayoutManager(this));

        rvUpcoming.setHasFixedSize(true);
        rvCompleted.setHasFixedSize(true);

        upcomingAdapter = new MatchesAdapter(this::openDetails);
        completedAdapter = new MatchesAdapter(this::openDetails);

        rvUpcoming.setAdapter(upcomingAdapter);
        rvCompleted.setAdapter(completedAdapter);
    }

    // Open detailed match overview screen passing document identification.
    private void openDetails(Match match) {
        Intent i = new Intent(this, MatchDetailsActivity.class);
        i.putExtra(MatchDetailsActivity.EXTRA_MATCH_ID, match.getId());
        startActivity(i);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Register active listeners when screen enters foreground.
        startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unregister active database listeners to release connections and prevent leaks.
        if (matchesListener != null) {
            matchesListener.remove();
        }
    }

    // Attach real-time Firestore database snapshot stream.
    private void startListening() {
        progressBar.setVisibility(View.VISIBLE);
        matchesListener = repository.getAllMatches(new MatchRepository.MatchesCallback() {
            @Override
            public void onLoaded(List<Match> matches) {
                progressBar.setVisibility(View.GONE);
                updateUI(matches);
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                Log.e("MatchesList", "Error loading matches", e);
                Toast.makeText(MatchesListActivity.this, "Failed to load matches", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Separate upcoming and completed matches before updating list adapter models.
    private void updateUI(List<Match> matches) {
        List<Match> upcoming = new ArrayList<>();
        List<Match> completed = new ArrayList<>();

        // Group matches according to play status enum.
        for (Match m : matches) {
            if (m.getStatus() == MatchStatus.UPCOMING) {
                upcoming.add(m);
            } else if (m.getStatus() == MatchStatus.PLAYED) {
                completed.add(m);
            }
        }

        upcomingAdapter.setItems(upcoming);
        completedAdapter.setItems(completed);

        tvNoUpcoming.setVisibility(upcoming.isEmpty() ? View.VISIBLE : View.GONE);
        tvNoCompleted.setVisibility(completed.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
