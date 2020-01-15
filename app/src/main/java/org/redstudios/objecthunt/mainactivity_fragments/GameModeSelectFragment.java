package org.redstudios.objecthunt.mainactivity_fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.redstudios.objecthunt.ClassifierActivity;
import org.redstudios.objecthunt.R;
import org.redstudios.objecthunt.model.AppState;
import org.redstudios.objecthunt.model.GameMode;
import org.redstudios.objecthunt.model.GameModeAdapter;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.DividerItemDecoration.VERTICAL;

/**
 * Fragment representing the login screen for Shrine.
 */
public class GameModeSelectFragment extends Fragment {

    private View view;
    private RecyclerView listOfGameModes;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.game_mode_select_fragment, container, false);
        ArrayList<GameMode> gameModesList = (ArrayList<GameMode>) AppState.get().getGameModesList();
        listOfGameModes = view.findViewById(R.id.game_modes_list_view);
        listOfGameModes.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(this.getActivity());
        listOfGameModes.setLayoutManager(layoutManager);

//        MaterialButton outdoorBtn = view.findViewById(R.id.game_mode_button_1);
//        MaterialButton indoorBtn = view.findViewById(R.id.game_mode_button_2);
//        MaterialButton officeBtn = view.findViewById(R.id.game_mode_button_3);

        GameModeAdapter gameModesAdapter = new GameModeAdapter(gameModesList, new GameModeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Integer position) {
                Intent intent = new Intent(getActivity(), ClassifierActivity.class);
                intent.putExtra("GameMode", gameModesList.get(position));
                startActivity(intent);
            }
        });

        listOfGameModes.setAdapter(gameModesAdapter);
        DividerItemDecoration itemDecor = new DividerItemDecoration(this.getActivity(), VERTICAL);
        listOfGameModes.addItemDecoration(itemDecor);
        Log.d("ANDREI", "SETTING");


//        outdoorBtn.setText(gameModesList.get(0).getGameModeName());
//        indoorBtn.setText(gameModesList.get(1).getGameModeName());
//        officeBtn.setText(gameModesList.get(2).getGameModeName());

//        outdoorBtn.setOnClickListener((View view) -> {
//            Intent intent = new Intent(getActivity(), ClassifierActivity.class);
//            intent.putExtra("GameMode", gameModesList.get(0));
//            startActivity(intent);
//        });
//
//        indoorBtn.setOnClickListener((View view) -> {
//            Intent intent = new Intent(getActivity(), ClassifierActivity.class);
//
//            intent.putExtra("GameMode", gameModesList.get(1));
//            startActivity(intent);
//        });
//
//        officeBtn.setOnClickListener((View view) -> {
//            Intent intent = new Intent(getActivity(), ClassifierActivity.class);
//
//            intent.putExtra("GameMode", gameModesList.get(2));
//            startActivity(intent);
//        });

        return view;
    }
}
