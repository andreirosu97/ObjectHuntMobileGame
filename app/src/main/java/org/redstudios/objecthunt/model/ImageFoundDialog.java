package org.redstudios.objecthunt.model;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.redstudios.objecthunt.R;

import androidx.appcompat.app.AppCompatActivity;

public class ImageFoundDialog extends AppCompatActivity {
    Button btn;
    TextView objectFoundT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_found_dialog);
        btn = findViewById(R.id.continue_button);
        objectFoundT = findViewById(R.id.object_found_text);
        btn.setOnClickListener((View v) -> finish());
        String objectName = getIntent().getExtras().getString("ObjectName");
        setTitle(getString(R.string.obj_found_dialog_title));
        objectFoundT.setText(objectName);
    }


}
