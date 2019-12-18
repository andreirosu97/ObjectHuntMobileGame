package org.redstudios.objecthunt.model;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;

import androidx.annotation.NonNull;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class AppState {
    private static AppState singletonObject;

    public static synchronized AppState get() {
        if (singletonObject == null) {
            singletonObject = new AppState();
        }
        return singletonObject;
    }

    private DocumentReference userDocument;

    //TODO convert this data into userAdapter for the database
    private String userId;
    private String nickName;
    private Double topScore;
    private HashMap<String, Object> objectsFound;

    public DocumentReference getUserDocument() {
        return userDocument;
    }

    @SuppressWarnings("unchecked")
    public void setUserDocument(DocumentReference userDocument) {
        this.userDocument = userDocument;
        userDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        userId = document.getId();
                        nickName = document.getString("nickName");
                        topScore = document.getDouble("topScore");
                        objectsFound = (HashMap<String, Object>) document.get("objectsFound");
                        Log.i("User Info", "User ID : " + userId);
                        Log.i("User Info", "Nickname : " + nickName);
                        Log.i("User Info", "TopScore : " + topScore);
                        for (String key : objectsFound.keySet()) {
                            Log.i("User Info", key + " was found " + objectsFound.get(key) + " times");
                        }

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public String getUserId() {
        return userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        //TODO update database
        this.nickName = nickName;
    }

    public Double getTopScore() {
        return topScore;
    }

    public void setTopScore(Double topScore) {
        //TODO update database
        this.topScore = topScore;
    }

    public HashMap<String, Object> getObjectsFound() {
        return objectsFound;
    }

    public void setObjectsFound(HashMap<String, Object> objectsFound) {
        //TODO update database
        this.objectsFound = objectsFound;
    }
}
