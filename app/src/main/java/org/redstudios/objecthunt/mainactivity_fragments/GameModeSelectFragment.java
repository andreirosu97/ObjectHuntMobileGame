package org.redstudios.objecthunt.mainactivity_fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.redstudios.objecthunt.ClassifierActivity;
import org.redstudios.objecthunt.R;
import org.redstudios.objecthunt.model.AppState;
import org.redstudios.objecthunt.model.GameMode;
import org.redstudios.objecthunt.model.GameModeAdapter;
import org.redstudios.objecthunt.utils.VerticalSpaceItemDecoration;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        GameModeAdapter gameModesAdapter = new GameModeAdapter(gameModesList, (Integer position) -> {
                Intent intent = new Intent(getActivity(), ClassifierActivity.class);
                intent.putExtra("GameMode", gameModesList.get(position));
                startActivity(intent);
        });

        listOfGameModes.setAdapter(gameModesAdapter);
        VerticalSpaceItemDecoration itemDecor = new VerticalSpaceItemDecoration(10);
        listOfGameModes.addItemDecoration(itemDecor);

        return view;
    }
}
