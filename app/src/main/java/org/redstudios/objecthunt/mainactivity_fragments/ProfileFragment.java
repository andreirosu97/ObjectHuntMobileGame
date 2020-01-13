package org.redstudios.objecthunt.mainactivity_fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
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

import org.redstudios.objecthunt.R;
import org.redstudios.objecthunt.model.AppState;
import org.redstudios.objecthunt.model.ObjectsAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class ProfileFragment extends Fragment {

    private ListView listScores;
    private ListView listTopScores;
    private ImageButton editButton;
    private TextView name;
    private TextView topScore;
    private EditText editName;
    private ViewSwitcher switcher;
    private FrameLayout touchInterceptor;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "Creating Profile view");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        listScores = view.findViewById(R.id.TopObjList);
        listTopScores = view.findViewById(R.id.topScores);
        editButton = view.findViewById(R.id.EditNameButton);
        name = view.findViewById(R.id.TextViewName);
        //topScore = view.findViewById(R.id.topScoreText);
        editName = view.findViewById(R.id.hiddenEditName);
        switcher = view.findViewById(R.id.nameSwitcher);
        touchInterceptor = view.findViewById(R.id.touchInterceptor);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated Profile Fragment");
        super.onActivityCreated(savedInstanceState);
        initializeListeners();
        updateProfileData();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeListeners() {
        editButton.setOnClickListener((View view) -> {
            View switchCheck = switcher.getCurrentView();
            if (switchCheck instanceof EditText) {
                editName.clearFocus();
            } else {
                switcher.showNext();
                editName.requestFocus();
            }
        });

        editName.setOnFocusChangeListener((View view, boolean hasFocus) -> {
            if (!hasFocus) {
                AppState.get().setNickName(editName.getText().toString());
                AppState.get().updatePlayerData();
                switcher.showPrevious();
                hideKeyboardFrom(view);
            } else {
                editName.setSelection(editName.getText().length());
                appearKeyboardFrom();
            }
        });

        touchInterceptor.setOnTouchListener((View v, MotionEvent event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (editName.isFocused()) {
                    Rect outRect = new Rect();
                    editName.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        editName.clearFocus();
                    }
                }
            }
            return false;
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void hideKeyboardFrom(View view) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void appearKeyboardFrom() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    private void updateProfileData() {
        if (getActivity() != null) {
            name.setText(AppState.get().getNickName());
            editName.setText(AppState.get().getNickName());

            List<Pair<String, String>> topScores = AppState.get().getPlayerScores();
            ObjectsAdapter topScoresAdapter = new ObjectsAdapter(this.getActivity(), R.layout.two_column_item_list, topScores);
            listTopScores.setAdapter(topScoresAdapter);

            List<Pair<String, String>> objectsFound = AppState.get().getListOfObjectsFound();
            ObjectsAdapter objectsFoundAdapter = new ObjectsAdapter(this.getActivity(), R.layout.two_column_item_list, objectsFound);
            listScores.setAdapter(objectsFoundAdapter);
        }
    }
}
