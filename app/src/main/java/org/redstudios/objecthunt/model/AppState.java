package org.redstudios.objecthunt.model;

import android.util.Log;
import android.util.Pair;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class AppState extends Observable {
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

    //TODO convert this data into userAdapter for the database
    private String userId;
    private String nickName;
    private Integer topScore;
    private HashMap<String, Object> objectsFound;

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
                Double doubleTopScore = document.getDouble("topScore");
                if (doubleTopScore != null) {
                    topScore = doubleTopScore.intValue();
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

    public void setFirebaseFirestore(FirebaseFirestore firebaseFirestore) {
        this.firebaseFirestore = firebaseFirestore;
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

    public Integer getTopScore() {
        return topScore;
    }

    public void setTopScore(Integer topScore) {
        this.topScore = topScore;
        updateDatabaseField("topScore", topScore);
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
}
