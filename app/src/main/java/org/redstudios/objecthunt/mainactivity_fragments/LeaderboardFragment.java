package org.redstudios.objecthunt.mainactivity_fragments;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.redstudios.objecthunt.R;
import org.redstudios.objecthunt.model.LeaderboardLVAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;


public class LeaderboardFragment extends Fragment {

    ListView listUsers;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.leaderboard_fragment, container, false);
        listUsers = view.findViewById(R.id.usersRank);

        List<Pair<String, Integer>> users = new ArrayList<>();
        users.add(Pair.create("Raul", 185));
        users.add(Pair.create("Caca", 300));
        users.add(Pair.create("Andrei", 175));
        users.add(Pair.create("Dusmanu face legea", 30));
        users.add(Pair.create("Flavius", 195));
        users.add(Pair.create("Darius", 165));
        users.add(Pair.create("Mine", 60));
        users.add(Pair.create("Paul", 200));
        users.add(Pair.create("M-a", 155));
        users.add(Pair.create("Facut", 145));
        users.add(Pair.create("Vine", 50));
        users.add(Pair.create("Mama", 135));
        users.add(Pair.create("Maca", 295));
        users.add(Pair.create("Banu", 40));
        users.add(Pair.create("Frumoasa", 125));
        users.add(Pair.create("Maii", 115));
        users.add(Pair.create("Eu sunt", 105));
        users.add(Pair.create("Capitanu", 90));
        users.add(Pair.create("Tot", 80));
        users.add(Pair.create("La", 70));

        LeaderboardLVAdapter objectsFoundAdapter = new LeaderboardLVAdapter(this.getActivity(), R.layout.leaderboard_row, users);
        listUsers.setAdapter(objectsFoundAdapter);
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.leader_board_header, listUsers, false);
        listUsers.addHeaderView(header);
        return view;
    }

}
