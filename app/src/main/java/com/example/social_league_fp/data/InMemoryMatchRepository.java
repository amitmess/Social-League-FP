package com.example.social_league_fp.data;

import com.example.social_league_fp.model.Match;
import com.example.social_league_fp.model.MatchStatus;

import java.util.ArrayList;
import java.util.List;

public class InMemoryMatchRepository {
    private static InMemoryMatchRepository instance;

    private final ArrayList<Match> matches = new ArrayList<>();

    private InMemoryMatchRepository() {
        seed();
    }

    public static synchronized InMemoryMatchRepository getInstance() {
        if (instance == null) instance = new InMemoryMatchRepository();
        return instance;
    }

    public List<Match> getAll() {
        return new ArrayList<>(matches);
    }

    public Match getById(String id) {
        for (Match m : matches) {
            if (m.getId().equals(id)) return m;
        }
        return null;
    }

    public boolean setScore(String matchId, int homeScore, int awayScore) {
        Match m = getById(matchId);
        if (m == null) return false;

        m.setHomeScore(homeScore);
        m.setAwayScore(awayScore);
        m.setStatus(MatchStatus.PLAYED);
        return true;
    }

    private void seed() {
        matches.clear();

        // Upcoming (עם confirmed/maybe)
        matches.add(new Match(
                "1", "Maccabi", "Hapoel", "Sun, Jan 28  3:00 PM", "Central Park Field",
                MatchStatus.UPCOMING, null, null,
                3, 1
        ));

        matches.add(new Match(
                "2", "Beitar", "Bnei Sakhnin", "Sun, Feb 4  4:00 PM", "Riverside Sports Complex",
                MatchStatus.UPCOMING, null, null,
                0, 0
        ));

        matches.add(new Match(
                "3", "Hapoel Haifa", "Maccabi Haifa", "Wed, Feb 7  9:00 PM", "Sammy Ofer",
                MatchStatus.UPCOMING, null, null,
                5, 2
        ));

        // Completed / Played (עם תוצאות)
        matches.add(new Match(
                "4", "Ashdod", "Netanya", "Sun, Jan 21  3:00 PM", "Yarkon Park Field 3",
                MatchStatus.PLAYED, 3, 2,
                8, 0
        ));

        matches.add(new Match(
                "5", "Kiryat Shmona", "Hadera", "Wed, Jan 10  5:30 PM", "Municipal",
                MatchStatus.PLAYED, 0, 0,
                2, 1
        ));

        // עוד משחקים (מומלץ 8–12)
        matches.add(new Match(
                "6", "Maccabi Petah Tikva", "Hapoel Beer Sheva", "Mon, Feb 12  8:00 PM", "HaMoshava",
                MatchStatus.UPCOMING, null, null,
                1, 0
        ));

        matches.add(new Match(
                "7", "Bnei Yehuda", "Ironi Kiryat Ata", "Sat, Jan 20  8:00 PM", "Hatikva",
                MatchStatus.PLAYED, 2, 1,
                6, 1
        ));

        matches.add(new Match(
                "8", "Hapoel Jerusalem", "Rishon LeZion", "Tue, Feb 18  7:30 PM", "Teddy",
                MatchStatus.UPCOMING, null, null,
                0, 0
        ));
    }
}