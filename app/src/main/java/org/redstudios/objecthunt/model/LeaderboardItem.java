package org.redstudios.objecthunt.model;

public class LeaderboardItem {
    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    private Integer color;
    private String displayName;
    private String displayScore;
    private String displayRank;

    public LeaderboardItem(String displayName, String displayScore, String displayRank) {
        this.color = 0;
        this.displayName = displayName;
        this.displayScore = displayScore;
        this.displayRank = displayRank;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayScore() {
        return displayScore;
    }

    public void setDisplayScore(String displayScore) {
        this.displayScore = displayScore;
    }

    public String getDisplayRank() {
        return displayRank;
    }

    public void setDisplayRank(String displayRank) {
        this.displayRank = displayRank;
    }
}
