package com.example.social_league_fp.ui.details;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.social_league_fp.R;
import com.example.social_league_fp.data.FirestoreMatchRepository;
import com.example.social_league_fp.data.MatchRepository;
import com.example.social_league_fp.model.Match;
import com.example.social_league_fp.model.MatchStatus;
import com.example.social_league_fp.ui.location.StadiumLocationActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.ListenerRegistration;

public class MatchDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_MATCH_ID = "extra_match_id";
    private static final String TAG = "MatchDetails";

    private Match match;
    private FirebaseAnalytics firebaseAnalytics;
    private MatchRepository repository;
    private ListenerRegistration matchListener;
    private String matchId;
    private boolean detailsEventLogged = false;

    private TextView tvTitle, tvDateTime, tvLocation, tvStatus, tvCurrentScore, tvAttendance;
    private EditText etHome, etAway;
    private Button btnSave, btnViewLocation;
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_details);

        repository = new FirestoreMatchRepository();
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        matchId = getIntent().getStringExtra(EXTRA_MATCH_ID);

        initViews();
        setupToolbar();

        if (matchId == null) {
            Toast.makeText(this, "Match ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvDetailsTitle);
        tvDateTime = findViewById(R.id.tvDetailsDateTime);
        tvLocation = findViewById(R.id.tvDetailsLocation);
        tvStatus = findViewById(R.id.tvDetailsStatus);
        tvCurrentScore = findViewById(R.id.tvDetailsCurrentScore);
        tvAttendance = findViewById(R.id.tvDetailsAttendance);
        etHome = findViewById(R.id.etHomeScore);
        etAway = findViewById(R.id.etAwayScore);
        btnSave = findViewById(R.id.btnSaveScore);
        btnViewLocation = findViewById(R.id.btnViewLocation);
        progressBar = findViewById(R.id.progressBarDetails); // Make sure this exists in layout
        
        btnSave.setOnClickListener(v -> submitScore());
        
        if (btnViewLocation != null) {
            btnViewLocation.setOnClickListener(v -> {
                if (match != null) {
                    Intent intent = new Intent(this, StadiumLocationActivity.class);
                    intent.putExtra("extra_match_id", match.getId());
                    intent.putExtra("extra_location", match.getLocation());
                    if (match.getLatitude() != null) {
                        intent.putExtra("extra_latitude", match.getLatitude());
                    }
                    if (match.getLongitude() != null) {
                        intent.putExtra("extra_longitude", match.getLongitude());
                    }
                    
                    // Log Analytics Event: open_location
                    Bundle anaBundle = new Bundle();
                    anaBundle.putString("match_id", match.getId());
                    firebaseAnalytics.logEvent("open_location", anaBundle);
                    
                    startActivity(intent);
                }
            });
        }
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                Bundle backBundle = new Bundle();
                backBundle.putString("from_screen", "match_details");
                firebaseAnalytics.logEvent("navigate_back", backBundle);
                finish();
            });
            toolbar.setTitle("Match Details");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (matchListener != null) {
            matchListener.remove();
        }
    }

    private void startListening() {
        if (matchId == null) {
            return;
        }
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        matchListener = repository.getMatchById(matchId, new MatchRepository.MatchCallback() {
            @Override
            public void onLoaded(Match loadedMatch) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                match = loadedMatch;
                updateUI();
            }

            @Override
            public void onError(Exception e) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading match", e);
                Toast.makeText(MatchDetailsActivity.this, "Error loading match", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (match == null) return;

        if (!detailsEventLogged) {
            Bundle detailsBundle = new Bundle();
            detailsBundle.putString("match_id", matchId);
            detailsBundle.putString("match_title", match.getTitle());
            detailsBundle.putString("match_status", match.getStatus().toString());
            firebaseAnalytics.logEvent("open_match_details", detailsBundle);
            detailsEventLogged = true;
        }

        if (toolbar != null) {
            toolbar.setSubtitle(match.getTitle());
        }

        tvTitle.setText(match.getTitle());
        tvDateTime.setText(match.getDateTime());
        tvLocation.setText(match.getLocation());
        tvStatus.setText(match.getStatus() == MatchStatus.PLAYED ? "Finished" : "Upcoming");
        tvCurrentScore.setText("Current score: " + match.getScoreText());

        if (tvAttendance != null) {
            tvAttendance.setText(match.getAttendanceText());
        }

        if (match.getHomeScore() != null && TextUtils.isEmpty(etHome.getText())) {
            etHome.setText(String.valueOf(match.getHomeScore()));
        }
        if (match.getAwayScore() != null && TextUtils.isEmpty(etAway.getText())) {
            etAway.setText(String.valueOf(match.getAwayScore()));
        }
    }

    private void submitScore() {
        String homeStr = etHome.getText().toString().trim();
        String awayStr = etAway.getText().toString().trim();

        if (TextUtils.isEmpty(homeStr) || TextUtils.isEmpty(awayStr)) {
            Toast.makeText(this, "Please enter both scores", Toast.LENGTH_SHORT).show();
            return;
        }

        int home, away;
        try {
            home = Integer.parseInt(homeStr);
            away = Integer.parseInt(awayStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Scores must be numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        if (home < 0 || away < 0) {
            Toast.makeText(this, "Scores must be >= 0", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.updateScore(matchId, home, away, () -> {
            Toast.makeText(this, "Score updated successfully", Toast.LENGTH_SHORT).show();
            logScoreEvent(home, away);
            finish();
        }, e -> {
            Log.e(TAG, "Failed to update score", e);
            Toast.makeText(this, "Failed to update score", Toast.LENGTH_SHORT).show();
        });
    }

    private void logScoreEvent(int home, int away) {
        Bundle scoreBundle = new Bundle();
        scoreBundle.putString("match_id", matchId);
        scoreBundle.putInt("home_score", home);
        scoreBundle.putInt("away_score", away);
        firebaseAnalytics.logEvent("submit_score", scoreBundle);
    }
}
