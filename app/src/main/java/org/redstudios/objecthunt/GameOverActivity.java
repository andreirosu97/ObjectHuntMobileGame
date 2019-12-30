package org.redstudios.objecthunt;

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
        Log.d("RAUL", "On create");
        super.onCreate(savedInstanceState);

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
        AppState.get().submitPlayerScore(gameMode, topPoints);
        AppState.get().setTopScore(gameMode, topPoints);

//        Log.d("RAUL", "Set objects ");
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
    }
}
