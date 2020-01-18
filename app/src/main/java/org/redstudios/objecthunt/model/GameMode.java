package org.redstudios.objecthunt.model;

import java.io.Serializable;
import java.util.ArrayList;

import androidx.annotation.NonNull;

public class GameMode implements Serializable, Comparable {

    private String gameModeName;

    public String getDescription() {
        return description;
    }

    private String description;
    private String leaderboardId;
    private Boolean leaderboardNeedUpdate;
    private ArrayList<String> objectList;

    public GameMode(String gameModeName, String leaderboardId, ArrayList<String> objectList, String description) {
        this.gameModeName = gameModeName;
        this.leaderboardId = leaderboardId;
        this.objectList = objectList;
        this.description = description;
        this.leaderboardNeedUpdate = true;
    }

    public String getGameModeName() {
        return gameModeName;
    }

    public void setGameModeName(String gameModeName) {
        this.gameModeName = gameModeName;
    }

    public String getLeaderboardId() {
        return leaderboardId;
    }

    public void setLeaderboardId(String leaderboardId) {
        this.leaderboardId = leaderboardId;
    }

    public ArrayList<String> getObjectList() {
        return objectList;
    }

    public void setObjectList(ArrayList<String> objectList) {
        this.objectList = objectList;
    }

    public void setLeaderboardNeedUpdate() {
        this.leaderboardNeedUpdate = true;
    }

    public void leaderboardUpdated() {
        this.leaderboardNeedUpdate = false;
    }

    public Boolean needsLeaderboardUpdate() {
        return leaderboardNeedUpdate;
    }

    @Override
    public int compareTo(@NonNull Object gm) {
        return gameModeName.compareTo(((GameMode) gm).gameModeName);
    }
}
