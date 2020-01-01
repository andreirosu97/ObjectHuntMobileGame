package org.redstudios.objecthunt.model;

import android.util.Log;
import android.util.Pair;

import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.ScoreSubmissionData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.redstudios.objecthunt.utils.CallbackableWithBoolean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.google.android.gms.games.leaderboard.LeaderboardVariant.COLLECTION_PUBLIC;
import static com.google.android.gms.games.leaderboard.LeaderboardVariant.TIME_SPAN_ALL_TIME;

public class AppState extends Observable {
    private static AppState singletonObject;
    private HashMap<String, ArrayList<String>> dataSets;


    public static final class GameModes {

        public static final String INDOOR = "indoor";
        public static final String OUTDOOR = "outdoor";
        public static final String OFFICE = "office";

        private GameModes() {
        }
    }

    private AppState() {
        needsUpdate.put(GameModes.INDOOR, true);
        needsUpdate.put(GameModes.OUTDOOR, true);
        needsUpdate.put(GameModes.OFFICE, true);
    }


    ArrayList<String> leaderboardsIds = new ArrayList<>(
            Arrays.asList("CgkI3s2wtYQeEAIQAQ", "CgkI3s2wtYQeEAIQAw", "CgkI3s2wtYQeEAIQBA"));

    public static synchronized AppState get() {
        if (singletonObject == null) {
            singletonObject = new AppState();
        }
        return singletonObject;
    }

    private DocumentReference userDocument;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser activeUser;
    private LeaderboardsClient leaderboardsClient;

    //TODO convert this data into userAdapter for the database
    private String userId;
    private String nickName;
    private HashMap<String, Object> topScore;
    private HashMap<String, Object> objectsFound;
    private List<LeaderboardItem> scores = new ArrayList<>();
    private HashMap<String, Boolean> needsUpdate = new HashMap<>();

    @SuppressWarnings("unchecked")
    public void setUserDocument(DocumentReference userDocument) {
        this.userDocument = userDocument;
        userDocument.addSnapshotListener((@Nullable DocumentSnapshot document,
                                          @Nullable FirebaseFirestoreException e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (document != null && document.exists()) {
                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                userId = document.getId();
                nickName = document.getString("nickName");
                topScore = (HashMap<String, Object>) document.get("topScore");
                for (String key : topScore.keySet()) {
                    Log.i("User Info", key + " has the score of " + topScore.get(key));
                    Long l = (Long) topScore.get(key);

                    submitPlayerScore(key, (int) (long) l);
                }
                objectsFound = (HashMap<String, Object>) document.get("objectsFound");
                Log.i("User Info", "User ID : " + userId);
                Log.i("User Info", "Nickname : " + nickName);
                Log.i("User Info", "TopScore : " + topScore);
                for (String key : objectsFound.keySet()) {
                    Log.i("User Info", key + " was found " + objectsFound.get(key) + " times");
                }
                this.setChanged();
                notifyObservers();
            } else {
                Log.d(TAG, "Current data: null");
            }
        });
    }

    public FirebaseUser getActiveUser() {
        return activeUser;
    }

    @SuppressWarnings("unchecked")
    public void setFirebaseFirestore(FirebaseFirestore firebaseFirestore) {
        this.firebaseFirestore = firebaseFirestore;
        dataSets = new HashMap<>();
        firebaseFirestore.collection("datasets")
                .get()
                .addOnCompleteListener((@NonNull Task<QuerySnapshot> task) -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.get("Objects"));
                                dataSets.put(document.getId(), (ArrayList<String>) document.get("Objects"));
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    public ArrayList<String> getGameModes() {
        return new ArrayList<>(Arrays.asList(GameModes.INDOOR, GameModes.OFFICE, GameModes.OUTDOOR));
    }

    public ArrayList<String> getObjectForGameMode(String gameMode) {
        return dataSets.get(gameMode);
    }

    public void setActiveUser(FirebaseUser activeUser) {
        this.activeUser = activeUser;
    }

    private void updateDatabaseField(String fieldName, Object value) {
        userDocument
                .update(fieldName, value)
                .addOnSuccessListener((Void aVoid) -> {
                    Log.i(TAG, "Success on updating the " + fieldName);
                    setChanged();
                    notifyObservers();
                })
                .addOnFailureListener((@NonNull Exception e) -> Log.e(TAG, "Failed on updating the " + fieldName));
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
        updateDatabaseField("nickName", nickName);
    }

    public void setTopScore(String gameMode, Integer newScore) {
        Long score = (Long) this.topScore.get(gameMode);
        if (score != null) {
            if (newScore > score) {
                this.topScore.put(gameMode, newScore);
            }
        } else {
            this.topScore.put(gameMode, newScore);
        }
        updateDatabaseField("topScore", this.topScore);
    }

    public void addToObjetsFound(List<String> objectsFound) {
        for (String objectName : objectsFound) {
            Long value = (Long) this.objectsFound.get(objectName);
            if (value != null) {
                this.objectsFound.put(objectName, value + 1);
            } else {
                this.objectsFound.put(objectName, 1);
            }
        }
        updateDatabaseField("objectsFound", this.objectsFound);
    }

    public List<Pair<String, String>> getListOfObjectsFound() {
        List<Pair<String, String>> objList = new ArrayList<>();
        for (String key : objectsFound.keySet()) {
            Object value = objectsFound.get(key);
            if (value != null) {
                objList.add(Pair.create(key, value.toString()));
            }
        }
        return objList;
    }

    public void loadLeaderBoard(String gameMode, CallbackableWithBoolean leaderboardDisplayerActivity) {
        String ldbId = "";
        scores = new ArrayList<>();

        switch (gameMode) {
            case GameModes.INDOOR:
                ldbId = leaderboardsIds.get(0);
                break;
            case GameModes.OUTDOOR:
                ldbId = leaderboardsIds.get(1);
                break;
            case GameModes.OFFICE:
                ldbId = leaderboardsIds.get(2);
                break;
        }

        leaderboardsClient.loadPlayerCenteredScores(ldbId, TIME_SPAN_ALL_TIME, COLLECTION_PUBLIC, 20, needsUpdate.get(gameMode)).addOnCompleteListener(
                (@NonNull Task<AnnotatedData<LeaderboardsClient.LeaderboardScores>> task) -> {
                    if (task.isSuccessful()) {

                        AnnotatedData<LeaderboardsClient.LeaderboardScores> annotatedData = task.getResult();
                        if (annotatedData != null) {
                            LeaderboardsClient.LeaderboardScores leaderboardScores = annotatedData.get();
                            if (leaderboardScores != null) {
                                LeaderboardScoreBuffer buffer = leaderboardScores.getScores();
                                for (LeaderboardScore score : buffer) {
                                    String name = score.getScoreHolderDisplayName();
                                    name = name.substring(0, name.length() - 4);
                                    scores.add(new LeaderboardItem(name, score.getDisplayScore(), score.getDisplayRank()));
                                }
                                buffer.release();
                                Log.d(TAG, "Success am luat boardul");
                                needsUpdate.put(gameMode, false);
                                leaderboardDisplayerActivity.callback(true);
                            } else {
                                Log.e(TAG, "Fail la board");
                            }
                        } else {
                            Log.e(TAG, "Fail la board");
                        }
                    } else {
                        Log.e(TAG, "Fail la board");
                    }
                });
    }

    public List<LeaderboardItem> getScores() {
        return scores;
    }

    public void submitPlayerScore(String gameMode, Integer score) {
        String ldbId = "";
        switch (gameMode) {
            case GameModes.INDOOR:
                ldbId = leaderboardsIds.get(0);
                break;
            case GameModes.OUTDOOR:
                ldbId = leaderboardsIds.get(1);
                break;
            case GameModes.OFFICE:
                ldbId = leaderboardsIds.get(2);
                break;
        }
        leaderboardsClient.submitScoreImmediate(ldbId, score).addOnCompleteListener(
                (@NonNull Task<ScoreSubmissionData> task) -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Success " + task.getResult().getLeaderboardId() + " " + task.getResult().getPlayerId());
                        this.setChanged();
                        notifyObservers();
                    } else {
                        Log.e(TAG, "Fail");
                    }
                });
    }

    public List<Pair<String, String>> getPlayerScores() {
        List<Pair<String, String>> scores = new ArrayList<>();
        for (String key : topScore.keySet()) {
            Object value = topScore.get(key);
            if (value != null) {
                scores.add(Pair.create(key, value.toString()));
            }
        }
        return scores;
    }

    public void setLeaderboardsClient(LeaderboardsClient leaderboardsClient) {
        this.leaderboardsClient = leaderboardsClient;
    }

    public FirebaseFirestore getFirebaseFirestore() {
        return firebaseFirestore;
    }

    public void setNeedsUpdate(String gameMode) {
        needsUpdate.put(gameMode, true);
    }
}
