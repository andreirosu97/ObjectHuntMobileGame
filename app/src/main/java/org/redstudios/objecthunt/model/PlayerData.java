package org.redstudios.objecthunt.model;

import java.util.HashMap;
import java.util.List;

public class PlayerData {
    private String nickName;
    private HashMap<String, Object> objectsFound;
    private HashMap<String, Object> topScore;

    public PlayerData() {
    }

    public PlayerData(String nickName, HashMap<String, Object> objectsFound, HashMap<String, Object> topScore) {
        this.nickName = nickName;
        this.objectsFound = objectsFound;
        this.topScore = topScore;
    }

    public String getNickName() {
        return nickName;
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

    public void submitPlayerScore(String gameMode, Integer newScore) {
        Long score = (Long) topScore.get(gameMode);
        if (score != null) {
            if (newScore > score) {
                topScore.put(gameMode, newScore);
            }
        } else {
            topScore.put(gameMode, newScore);
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

    @Override
    public String toString() {
        return "PlayerData{" +
                "nickName='" + nickName + '\'' +
                ", objectsFound=" + objectsFound +
                ", topScore=" + topScore +
                '}';
    }
}
