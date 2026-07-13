package com.example.social_league_fp.model;

public class Match {
    private final String id;
    private final String homeTeam;
    private final String awayTeam;
    private final String dateTime;
    private final String location;

    private MatchStatus status;
    private Integer homeScore; // nullable
    private Integer awayScore; // nullable

    private int confirmedCount;
    private int maybeCount;

    private Double latitude;
    private Double longitude;

    /**
     * Complete constructor including coordinates
     */
    public Match(String id,
                 String homeTeam,
                 String awayTeam,
                 String dateTime,
                 String location,
                 MatchStatus status,
                 Integer homeScore,
                 Integer awayScore,
                 int confirmedCount,
                 int maybeCount,
                 Double latitude,
                 Double longitude) {

        this.id = id;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.dateTime = dateTime;
        this.location = location;
        this.status = status;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.confirmedCount = confirmedCount;
        this.maybeCount = maybeCount;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Constructor without coordinates
     */
    public Match(String id,
                 String homeTeam,
                 String awayTeam,
                 String dateTime,
                 String location,
                 MatchStatus status,
                 Integer homeScore,
                 Integer awayScore,
                 int confirmedCount,
                 int maybeCount) {

        this(id, homeTeam, awayTeam, dateTime, location, status, homeScore, awayScore, confirmedCount, maybeCount, null, null);
    }

    /**
     * Compatibility constructor
     */
    public Match(String id,
                 String homeTeam,
                 String awayTeam,
                 String dateTime,
                 String location,
                 MatchStatus status,
                 Integer homeScore,
                 Integer awayScore) {

        this(id, homeTeam, awayTeam, dateTime, location, status, homeScore, awayScore, 0, 0, null, null);
    }

    public String getId() { return id; }
    public String getHomeTeam() { return homeTeam; }
    public String getAwayTeam() { return awayTeam; }
    public String getDateTime() { return dateTime; }
    public String getLocation() { return location; }

    public MatchStatus getStatus() { return status; }
    public Integer getHomeScore() { return homeScore; }
    public Integer getAwayScore() { return awayScore; }

    public void setStatus(MatchStatus status) { this.status = status; }
    public void setHomeScore(Integer homeScore) { this.homeScore = homeScore; }
    public void setAwayScore(Integer awayScore) { this.awayScore = awayScore; }

    public int getConfirmedCount() { return confirmedCount; }
    public int getMaybeCount() { return maybeCount; }
    public void setConfirmedCount(int confirmedCount) { this.confirmedCount = confirmedCount; }
    public void setMaybeCount(int maybeCount) { this.maybeCount = maybeCount; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getTitle() {
        return homeTeam + " vs " + awayTeam;
    }

    public String getScoreText() {
        if (homeScore == null || awayScore == null) return "—";
        return homeScore + " - " + awayScore;
    }

    public String getAttendanceText() {
        if (maybeCount > 0) {
            return confirmedCount + " confirmed · " + maybeCount + " maybe";
        }
        return confirmedCount + " confirmed";
    }
}