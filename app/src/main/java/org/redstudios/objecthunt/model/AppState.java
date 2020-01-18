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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.google.android.gms.games.leaderboard.LeaderboardVariant.COLLECTION_PUBLIC;
import static com.google.android.gms.games.leaderboard.LeaderboardVariant.TIME_SPAN_ALL_TIME;

public class AppState {
    private static AppState singletonObject;

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
    private HashMap<String, GameMode> gameModes = new HashMap<>();

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
                    GameMode gm = gameModes.get(key);
                    if (gm != null && l != null) {
                        submitPlayerScore(gm, (int) (long) l, false, null);
                    }
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
    public void setFirebaseFirestore(FirebaseFirestore firebaseFirestore, CallbackableWithBoolean callbackClass) {
        Log.d("AppState", "Fetching datasets");
        this.firebaseFirestore = firebaseFirestore;
        firebaseFirestore.collection("datasets")
                .get()
                .addOnCompleteListener((@NonNull Task<QuerySnapshot> task) -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("AppState", document.getId() + " => " + document.get("Objects"));
                                gameModes.put(document.getId(),
                                        new GameMode(
                                                document.getId(),
                                                (String) document.get("leaderboardId"),
                                                (ArrayList<String>) document.get("Objects"),
                                                (String) document.get("Description"))
                                );
                            }
                            callbackClass.callback(true);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        callbackClass.callback(false);
                    }
                });
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

    public void setTopScore(GameMode gameMode, Integer newScore) {
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
        return sortPairStrintString(objList);
    }

    public void loadLeaderBoard(GameMode gameMode, CallbackableWithBoolean leaderboardDisplayerActivity) {
        scores = new ArrayList<>();
        leaderboardsClient.loadPlayerCenteredScores(gameMode.getLeaderboardId(), TIME_SPAN_ALL_TIME,
                COLLECTION_PUBLIC, 20, gameMode.needsLeaderboardUpdate())
                .addOnCompleteListener(
                (@NonNull Task<AnnotatedData<LeaderboardsClient.LeaderboardScores>> task) -> {
                    if (task.isSuccessful()) {
                        AnnotatedData<LeaderboardsClient.LeaderboardScores> annotatedData = task.getResult();
                        if (annotatedData != null) {
                            LeaderboardsClient.LeaderboardScores leaderboardScores = annotatedData.get();
                            if (leaderboardScores != null) {
                                LeaderboardScoreBuffer buffer = leaderboardScores.getScores();
                                for (LeaderboardScore score : buffer) {
                                    String name = score.getScoreHolderDisplayName();
                                    scores.add(new LeaderboardItem(name, score.getDisplayScore(), score.getDisplayRank()));
                                }
                                buffer.release();
                                Log.d(TAG, "Success am luat boardul");
                                gameMode.leaderboardUpdated();
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

    public void submitPlayerScore(GameMode gameMode, Integer score, Boolean showMessage, Activity activity) {
        leaderboardsClient.submitScoreImmediate(gameMode.getLeaderboardId(), score).addOnCompleteListener(
                (@NonNull Task<ScoreSubmissionData> task) -> {
                    if (task.isSuccessful()) {
                        Log.d("AppState subscore", "Success " + task.getResult().getLeaderboardId() + " " + task.getResult().getScoreResult(TIME_SPAN_ALL_TIME) + " " + task.getResult().getPlayerId());
                        if (showMessage) {
                            Toast.makeText(activity, "Successfully updated the leaderboard.", Toast.LENGTH_SHORT).show();
                            gameMode.setLeaderboardNeedUpdate();
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
        return sortPairStrintString(scores);
    }

    private List<Pair<String, String>> sortPairStrintString(List<Pair<String, String>> userRank) {
        Collections.sort(userRank, (Pair<String, String> t0, Pair<String, String> t1) -> -Integer.compare(Integer.parseInt(t0.second), Integer.parseInt(t1.second)));
        return userRank;
    }

    public void setLeaderboardsClient(LeaderboardsClient leaderboardsClient) {
        this.leaderboardsClient = leaderboardsClient;
    }

    public FirebaseFirestore getFirebaseFirestore() {
        return firebaseFirestore;
    }

    @SuppressWarnings("unchecked")
    public List<GameMode> getGameModesList() {
        List<GameMode> gmds = new ArrayList<>(gameModes.values());
        Collections.sort(gmds);
        return gmds;
    }

    public List<String> getGameModesNames() {
        return new ArrayList<>(gameModes.keySet());
    }

    public GameMode getRandomGameMode() {
        List<GameMode> gmds = getGameModesList();
        Integer index = Math.abs((new Random(System.currentTimeMillis() % 5000).nextInt() % gmds.size()));
        return gmds.get(index);
    }

    //This should never be used only in extreme cases
    public GameMode getGameModeByName(String name) {
        return gameModes.get(name);
    }
}
