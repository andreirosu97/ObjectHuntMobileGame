package org.redstudios.objecthunt.mainactivity_fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import org.redstudios.objecthunt.ClassifierActivity;
import org.redstudios.objecthunt.R;

import java.util.Objects;

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

        MaterialButton nextButton = view.findViewById(R.id.game_mode_button_1);
        MaterialButton nextButton2 = view.findViewById(R.id.game_mode_button_2);

        nextButton.setOnClickListener((View view) -> {
            Intent intent = new Intent(getActivity(), ClassifierActivity.class);
            startActivity(intent);
        });

        nextButton2.setOnClickListener((View view) -> {
                ((BottomNavigationView) Objects.requireNonNull(getActivity()).findViewById(R.id.bottom_navigation)).getSelectedItemId();
        });


        return view;
    }
}
