package org.redstudios.objecthunt.mainactivity_fragments;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.redstudios.objecthunt.R;
import org.redstudios.objecthunt.model.AppState;
import org.redstudios.objecthunt.model.ObjectsAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class ProfileFragment extends Fragment {

    private ListView listScores;
    private TextView noScores;
    private TextView noTopScores;
    private TextView bestTime;
    private TextView totalTime;
    private ListView listTopScores;
    private TextView name;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "Creating Profile view");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        listScores = view.findViewById(R.id.TopObjList);
        noTopScores = view.findViewById(R.id.NoScoresTextView);
        noScores = view.findViewById(R.id.NoObjectsTextView);
        listTopScores = view.findViewById(R.id.topScores);
        name = view.findViewById(R.id.TextViewName);
        bestTime = view.findViewById(R.id.TextViewBestTime);
        totalTime = view.findViewById(R.id.TextViewTotalTime);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated Profile Fragment");
        super.onActivityCreated(savedInstanceState);
        updateProfileData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void updateProfileData() {
        if (getActivity() != null) {
            name.setText(AppState.get().getNickName());
            bestTime.setText(AppState.get().getBestTime());
            totalTime.setText(AppState.get().getTotalTime());

            List<Pair<String, String>> topScores = AppState.get().getPlayerScores();
            if (topScores.size() == 0) {
                noTopScores.setVisibility(View.VISIBLE);
                listTopScores.setVisibility(View.GONE);
            } else {
                noTopScores.setVisibility(View.GONE);
                listTopScores.setVisibility(View.VISIBLE);
                ObjectsAdapter topScoresAdapter = new ObjectsAdapter(this.getActivity(), R.layout.two_column_item_list, topScores);
                listTopScores.setAdapter(topScoresAdapter);
            }

            List<Pair<String, String>> objectsFound = AppState.get().getListOfObjectsFound();

            if (objectsFound.size() == 0) {
                noScores.setVisibility(View.VISIBLE);
                listScores.setVisibility(View.GONE);
            } else {
                noScores.setVisibility(View.GONE);
                listScores.setVisibility(View.VISIBLE);
                ObjectsAdapter objectsFoundAdapter = new ObjectsAdapter(this.getActivity(), R.layout.two_column_item_list, objectsFound);
                listScores.setAdapter(objectsFoundAdapter);
            }
        }
    }
}
