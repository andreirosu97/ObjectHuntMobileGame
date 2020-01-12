package org.redstudios.objecthunt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.redstudios.objecthunt.model.AppState;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {
    private ListView objFoundList;
    private TextView textPoints;
    private TextView textObjetcts;
    private Button challengeButton;
    private Button playButton;
    private Button backButton;
    private Integer topPoints;
    private String gameMode;
    private ArrayList<String> foundObjects;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("AppState", "Creating game over Activity");
        Bundle gameResult = getIntent().getExtras();
        setContentView(R.layout.game_over_activity);
        objFoundList = findViewById(R.id.ObjFoundList);
        textObjetcts = findViewById(R.id.TextViewNrObj);
        textPoints = findViewById(R.id.TextViewPoints);
        challengeButton = findViewById(R.id.challengeButton);
        playButton = findViewById(R.id.playButton);
        backButton = findViewById(R.id.backButton);

//        Log.d("RAUL", "Getting results");
        foundObjects = gameResult.getStringArrayList("FoundObjects");
        AppState.get().addToObjetsFound(foundObjects);
//        Log.d("RAUL", "Create adapter");
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, R.layout.centered_listview_item, foundObjects);
//        Log.d("RAUL", "Set adapter");
        objFoundList.setAdapter(listAdapter);

//        Log.d("RAUL", "Points ");
        topPoints = gameResult.getInt("Points");
//        Log.d("RAUL", "Set points ");
        textPoints.setText(topPoints.toString());

        gameMode = gameResult.getString("GameMode");
        AppState.get().submitPlayerScore(gameMode, topPoints, true, this);
        AppState.get().setTopScore(gameMode, topPoints);
        AppState.get().setNeedsUpdate(gameMode);

        textObjetcts.setText(Integer.toString(foundObjects.size()));

        playButton.setOnClickListener((View view) -> {
                    setResult(RESULT_OK, null);
                    finish();
                }
        );

        backButton.setOnClickListener((View view) -> {
                    setResult(RESULT_CANCELED, null);
                    finish();
                }
        );

        challengeButton.setOnClickListener((View view) -> {
            try {
                String shareBody = "Hey, I challenge you to beat by score of " + topPoints + " points in the " + gameMode + " mode. Here is the app check it out : <link to app>";
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "I challenge you !");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            } catch (Exception e) {
                //e.toString();
            }
        });
    }
}
