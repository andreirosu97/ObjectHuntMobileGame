package org.redstudios.objecthunt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;


public class ProfileFragment extends Fragment {

    ListView listScores;
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView l2 = (ListView) getActivity().findViewById(R.id.TopObjList);
        List<String> arr2 = new ArrayList<>();
        arr2.add("Masina");
        arr2.add("Telefon");
        arr2.add("Casca");
        arr2.add("Mouse");
        arr2.add("Monitor");
        ArrayAdapter<String> l2Adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arr2);
        l2.setAdapter(l2Adapter);

    }
}
