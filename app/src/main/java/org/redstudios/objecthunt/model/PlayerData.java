package org.redstudios.objecthunt.model;

import java.util.HashMap;
import java.util.List;

public class PlayerData {
    private String nickName;
    private long bestTime;
    private long totalTime;
    private HashMap<String, Object> objectsFound;
    private HashMap<String, Object> topScore;

    public PlayerData() {
    }

    public PlayerData(String nickName, long bestTime, long totalTime, HashMap<String, Object> objectsFound, HashMap<String, Object> topScore) {
        this.nickName = nickName;
        this.bestTime = bestTime;
        this.totalTime = totalTime;
        this.objectsFound = objectsFound;
        this.topScore = topScore;
    }

    public String getNickName() {
        return nickName;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getBestTime() {
        return bestTime;
    }

    public HashMap<String, Object> getObjectsFound() {
        return objectsFound;
    }

    public HashMap<String, Object> getTopScore() {
        return topScore;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setObjectsFound(HashMap<String, Object> objectsFound) {
        this.objectsFound = objectsFound;
    }

    public void setTopScore(HashMap<String, Object> topScore) {
        this.topScore = topScore;
    }

    public void submitPlayerScore(GameMode gameMode, Integer newScore) {
        String gameModeName = gameMode.getGameModeName();
        Long score = (Long) topScore.get(gameModeName);
        if (score != null) {
            if (newScore > score) {
                topScore.put(gameModeName, newScore);
            }
        } else {
            topScore.put(gameModeName, newScore);
        }
    }

    public void addFoundObjects(List<String> objectsFound) {
        for (String objectName : objectsFound) {
            Long value = (Long) this.objectsFound.get(objectName);
            if (value != null) {
                this.objectsFound.put(objectName, value + 1);
            } else {
                this.objectsFound.put(objectName, 1);
            }
        }
    }

    public void setBestTime(long bestTime) {
        if (bestTime > this.bestTime)
            this.bestTime = bestTime;
    }

    public void addTime(long time) {
        totalTime = totalTime + time;
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "nickName='" + nickName + '\'' +
                ", bestTime='" + bestTime + '\'' +
                ", totalTime='" + totalTime + '\'' +
                ", objectsFound=" + objectsFound +
                ", topScore=" + topScore +
                '}';
    }
}
