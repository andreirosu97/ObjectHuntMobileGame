package org.redstudios.objecthunt.mainactivity_fragments;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.redstudios.objecthunt.model.ObjectsAdapter;
import org.redstudios.objecthunt.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;


public class ProfileFragment extends Fragment {

    ListView listScores;
    ImageButton editButton;
    TextView name;
    EditText editName;
    ViewSwitcher switcher;
    View view;
    FrameLayout touchInterceptor;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.profile_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listScores = (ListView) getActivity().findViewById(R.id.TopObjList);
        editButton = (ImageButton) getActivity().findViewById(R.id.EditNameButton);
        name = (TextView) getActivity().findViewById(R.id.TextViewName);
        editName = (EditText) getActivity().findViewById(R.id.hiddenEditName);
        switcher = (ViewSwitcher) getActivity().findViewById(R.id.nameSwitcher);
        touchInterceptor = (FrameLayout) getActivity().findViewById(R.id.touchInterceptor);

        List<Pair<String,String>> arr2 = new ArrayList<>();
        arr2.add(Pair.create("Masina", "300"));
        arr2.add(Pair.create("Telefon", "200"));
        arr2.add(Pair.create("Casca", "100"));
        arr2.add(Pair.create("Mouse", "420"));
        arr2.add(Pair.create("Monitor", "690"));
        ObjectsAdapter l2Adapter = new ObjectsAdapter(getActivity(), R.layout.two_column_item_list, arr2);
        listScores.setAdapter(l2Adapter);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switcher.showNext();
                editName.requestFocus();
            }
        });

        editName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    name.setText(editName.getText());
                    switcher.showPrevious();
                    hideKeyboardFrom(view);
                }
                else
                    appearKeyboardFrom(view);
            }
        });

        touchInterceptor.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (editName.isFocused()) {
                        Rect outRect = new Rect();
                        editName.getGlobalVisibleRect(outRect);
                        if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                            editName.clearFocus();
                        }
                    }
                }
                return false;
            }
        });
    }

    public void hideKeyboardFrom(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public void appearKeyboardFrom(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }
}
