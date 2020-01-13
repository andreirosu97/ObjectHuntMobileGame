package org.redstudios.objecthunt.mainactivity_fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;

import org.redstudios.objecthunt.ClassifierActivity;
import org.redstudios.objecthunt.R;
import org.redstudios.objecthunt.model.AppState;
import org.redstudios.objecthunt.model.GameMode;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * Fragment representing the login screen for Shrine.
 */
public class GameModeSelectFragment extends Fragment {

    private View view;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.game_mode_select_fragment, container, false);
        List<GameMode> gms = AppState.get().getGameModesList();

        MaterialButton outdoorBtn = view.findViewById(R.id.game_mode_button_1);
        MaterialButton indoorBtn = view.findViewById(R.id.game_mode_button_2);
        MaterialButton officeBtn = view.findViewById(R.id.game_mode_button_3);
        outdoorBtn.setText(gms.get(0).getGameModeName());
        indoorBtn.setText(gms.get(1).getGameModeName());
        officeBtn.setText(gms.get(2).getGameModeName());

        outdoorBtn.setOnClickListener((View view) -> {
            Intent intent = new Intent(getActivity(), ClassifierActivity.class);
            intent.putExtra("GameMode", gms.get(0));
            startActivity(intent);
        });

        indoorBtn.setOnClickListener((View view) -> {
            Intent intent = new Intent(getActivity(), ClassifierActivity.class);

            intent.putExtra("GameMode", gms.get(1));
            startActivity(intent);
        });

        officeBtn.setOnClickListener((View view) -> {
            Intent intent = new Intent(getActivity(), ClassifierActivity.class);

            intent.putExtra("GameMode", gms.get(2));
            startActivity(intent);
        });

        return view;
    }
}
