package org.redstudios.objecthunt.model;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.google.android.gms.games.leaderboard.LeaderboardVariant.COLLECTION_PUBLIC;
import static com.google.android.gms.games.leaderboard.LeaderboardVariant.TIME_SPAN_ALL_TIME;

public class AppState {
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

    private ArrayList<String> leaderboardsIds = new ArrayList<>(
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

    private PlayerData playerData;
    private List<LeaderboardItem> scores = new ArrayList<>();
    private HashMap<String, Boolean> needsUpdate = new HashMap<>();

    @SuppressWarnings("unchecked")
    public void setUserDocument(DocumentReference userDocument) {
        this.userDocument = userDocument;
        userDocument.addSnapshotListener((@Nullable DocumentSnapshot document,
                                          @Nullable FirebaseFirestoreException e) -> {
            String nickName;
            HashMap<String, Object> topScore;
            HashMap<String, Object> objectsFound;

            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (document != null && document.exists()) {
                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                nickName = document.getString("nickName");
                objectsFound = (HashMap<String, Object>) document.get("objectsFound");
                topScore = (HashMap<String, Object>) document.get("topScore");

                playerData = new PlayerData(nickName, objectsFound, topScore);

                for (String key : topScore.keySet()) {
                    Log.i("User Info", key + " has the score of " + topScore.get(key));
                    Long l = (Long) topScore.get(key);
                    submitPlayerScore(key, (int) (long) l, false, null);
                }

                Log.d("User Info", playerData.toString());
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

    public void updatePlayerData() { //Updates the server side database
        userDocument
                .set(playerData)
                .addOnSuccessListener((Void aVoid) -> {
                    Log.d(TAG, "Success on updating playerdata " + playerData.toString());
                })
                .addOnFailureListener((@NonNull Exception e) -> Log.e(TAG, "Failed on updating the player data : " + e));
    }

    public String getNickName() {
        return playerData.getNickName();
    }

    public void setNickName(String nickName) {
        playerData.setNickName(nickName);
    }

    public void setTopScore(String gameMode, Integer newScore) {
        playerData.submitPlayerScore(gameMode, newScore);
    }

    public void addToObjetsFound(List<String> objectsFound) {
        playerData.addFoundObjects(objectsFound);
    }

    public List<Pair<String, String>> getListOfObjectsFound() {
        List<Pair<String, String>> objList = new ArrayList<>();
        for (String key : playerData.getObjectsFound().keySet()) {
            Object value = playerData.getObjectsFound().get(key);
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

    public void submitPlayerScore(String gameMode, Integer score, Boolean showMessage, Activity activity) {
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
        if (ldbId.equals("")) {
            return;
        }
        leaderboardsClient.submitScoreImmediate(ldbId, score).addOnCompleteListener(
                (@NonNull Task<ScoreSubmissionData> task) -> {
                    if (task.isSuccessful()) {
                        Log.d("AppState subscore", "Success " + task.getResult().getLeaderboardId() + " " + task.getResult().getScoreResult(TIME_SPAN_ALL_TIME) + " " + task.getResult().getPlayerId());
                        if (showMessage) {
                            Toast.makeText(activity, "Successfully updated the leaderboard.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("AppState subscore", "Fail " + task.getException().toString());
                    }
                });
    }

    public List<Pair<String, String>> getPlayerScores() {
        List<Pair<String, String>> scores = new ArrayList<>();
        for (String key : playerData.getTopScore().keySet()) {
            Object value = playerData.getTopScore().get(key);
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
