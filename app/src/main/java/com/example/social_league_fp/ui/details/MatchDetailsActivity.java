package com.example.social_league_fp.ui.details;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.social_league_fp.R;
import com.example.social_league_fp.data.InMemoryMatchRepository;
import com.example.social_league_fp.model.Match;
import com.example.social_league_fp.model.MatchStatus;
import com.google.android.material.appbar.MaterialToolbar;

public class MatchDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_MATCH_ID = "extra_match_id";

    private Match match;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_details);

        // Toolbar back arrow + titles
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
            toolbar.setTitle("Match Details");
            toolbar.setSubtitle(" "); // יתעדכן אחרי טעינת match
        }

        String matchId = getIntent().getStringExtra(EXTRA_MATCH_ID);
        match = InMemoryMatchRepository.getInstance().getById(matchId);

        if (match == null) {
            Toast.makeText(this, "Match not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // עדכון תת-כותרת אחרי שיש match
        if (toolbar != null) {
            toolbar.setSubtitle(match.getTitle()); // "Team A vs Team B"
        }

        TextView tvTitle = findViewById(R.id.tvDetailsTitle);
        TextView tvDateTime = findViewById(R.id.tvDetailsDateTime);
        TextView tvLocation = findViewById(R.id.tvDetailsLocation);
        TextView tvStatus = findViewById(R.id.tvDetailsStatus);
        TextView tvCurrentScore = findViewById(R.id.tvDetailsCurrentScore);

        EditText etHome = findViewById(R.id.etHomeScore);
        EditText etAway = findViewById(R.id.etAwayScore);
        Button btnSave = findViewById(R.id.btnSaveScore);

        tvTitle.setText(match.getTitle());
        tvDateTime.setText(match.getDateTime());
        tvLocation.setText(match.getLocation());
        tvStatus.setText(match.getStatus() == MatchStatus.PLAYED ? "Finished" : "Upcoming");
        tvCurrentScore.setText("Current score: " + match.getScoreText());

        // אם כבר יש תוצאה – נציג בשדות
        if (match.getHomeScore() != null) etHome.setText(String.valueOf(match.getHomeScore()));
        if (match.getAwayScore() != null) etAway.setText(String.valueOf(match.getAwayScore()));

        btnSave.setOnClickListener(v -> {
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

            boolean ok = InMemoryMatchRepository.getInstance().setScore(match.getId(), home, away);
            if (!ok) {
                Toast.makeText(this, "Failed to save score", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Score saved", Toast.LENGTH_SHORT).show();
            finish(); // חוזרים לרשימה; onResume ירענן
        });
    }
}