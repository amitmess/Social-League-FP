package com.example.social_league_fp.ui.matches;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_league_fp.R;
import com.example.social_league_fp.data.InMemoryMatchRepository;
import com.example.social_league_fp.model.Match;
import com.example.social_league_fp.model.MatchStatus;
import com.example.social_league_fp.ui.details.MatchDetailsActivity;

import java.util.ArrayList;
import java.util.List;

public class MatchesListActivity extends AppCompatActivity {

    private MatchesAdapter upcomingAdapter;
    private MatchesAdapter completedAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches_list);

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

    private void openDetails(Match match) {
        Intent i = new Intent(this, MatchDetailsActivity.class);
        i.putExtra(MatchDetailsActivity.EXTRA_MATCH_ID, match.getId());
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();

        List<Match> all = InMemoryMatchRepository.getInstance().getAll();
        if (all == null) all = new ArrayList<>();

        List<Match> upcoming = new ArrayList<>();
        List<Match> completed = new ArrayList<>();

        for (Match m : all) {
            if (m.getStatus() == MatchStatus.UPCOMING) {
                upcoming.add(m);
            } else {
                completed.add(m);
            }
        }

        upcomingAdapter.setItems(upcoming);
        completedAdapter.setItems(completed);
    }
}