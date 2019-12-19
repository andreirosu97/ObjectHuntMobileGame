package org.redstudios.objecthunt;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class GameOverActivity extends AppCompatActivity {
    private ListView objFoundList;
    private Button challengeButton;
    private Button playButton;
    private Button backButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_over_activity);

        objFoundList = findViewById(R.id.ObjFoundList);
        List<String> objArray = new ArrayList<>();
        objArray.add("Masina");
        objArray.add("Telefon");
        objArray.add("Casca");
        objArray.add("Mouse");
        objArray.add("Monitor");
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, R.layout.centered_listview_item, objArray);
        objFoundList.setAdapter(listAdapter);

        challengeButton = findViewById(R.id.challengeButton);
        playButton = findViewById(R.id.playButton);
        backButton = findViewById(R.id.backButton);

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
