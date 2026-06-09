package com.example.social_league_fp.data;

import com.example.social_league_fp.model.Match;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;

public interface MatchRepository {
    interface MatchesCallback {
        void onLoaded(List<Match> matches);
        void onError(Exception e);
    }

    interface MatchCallback {
        void onLoaded(Match match);
        void onError(Exception e);
    }

    ListenerRegistration getAllMatches(MatchesCallback callback);
    ListenerRegistration getMatchById(String id, MatchCallback callback);
    void updateScore(String id, int homeScore, int awayScore, Runnable onSuccess, OnError onError);

    interface OnError {
        void onError(Exception e);
    }
}
