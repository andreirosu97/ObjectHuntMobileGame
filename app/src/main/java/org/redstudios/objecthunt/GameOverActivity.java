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
import org.redstudios.objecthunt.model.GameMode;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {
    private Integer topPoints;
    private GameMode gameMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("AppState", "Creating game over Activity");
        Bundle gameResult = getIntent().getExtras();
        setContentView(R.layout.game_over_activity);
        ListView objFoundList = findViewById(R.id.ObjFoundList);
        TextView textObjetcts = findViewById(R.id.TextViewNrObj);
        TextView textPoints = findViewById(R.id.TextViewPoints);
        TextView foundObjectText = findViewById(R.id.object_found_text);
        Button challengeButton = findViewById(R.id.challengeButton);
        Button playButton = findViewById(R.id.playButton);
        Button backButton = findViewById(R.id.backButton);

        if (gameResult != null) {
            ArrayList<String> foundObjects = gameResult.getStringArrayList("FoundObjects");

            AppState.get().addToObjetsFound(foundObjects);

            if (foundObjects != null && foundObjects.size() > 0) {
                ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, R.layout.centered_listview_item, foundObjects);
                objFoundList.setAdapter(listAdapter);
                objFoundList.setVisibility(View.VISIBLE);
                foundObjectText.setVisibility(View.VISIBLE);
                String stringFoundObjectNumber = Integer.toString(foundObjects.size());
                textObjetcts.setText(stringFoundObjectNumber);
            } else {
                objFoundList.setVisibility(View.GONE);
                foundObjectText.setVisibility(View.GONE);
                textObjetcts.setText("0");
            }

            topPoints = gameResult.getInt("Points");

            if (topPoints > 0) {
                String stringTopPoints = topPoints.toString();
                textPoints.setText(stringTopPoints);

                gameMode = (GameMode) gameResult.getSerializable("GameMode");
                AppState.get().submitPlayerScore(gameMode, topPoints, true, this);
                AppState.get().setTopScore(gameMode, topPoints);
                AppState.get().updatePlayerData();
            } else {
                textPoints.setText("0");
            }
        }

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
                String shareBody = "Hey, I challenge you to beat by score of " + topPoints + " points in the " + gameMode + " mode. Download it now from the app store!";
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
