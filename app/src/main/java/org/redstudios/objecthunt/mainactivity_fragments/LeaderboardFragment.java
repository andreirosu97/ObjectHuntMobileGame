package org.redstudios.objecthunt.mainactivity_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.redstudios.objecthunt.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class LeaderboardFragment extends Fragment {

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.leaderboard_fragment, container, false);
        return view;
    }

}
