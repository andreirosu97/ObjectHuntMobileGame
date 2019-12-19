package org.redstudios.objecthunt.mainactivity_fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
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

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class ProfileFragment extends Fragment {

    private ListView listScores;
    private ImageButton editButton;
    private TextView name;
    private TextView topScore;
    private EditText editName;
    private ViewSwitcher switcher;
    private View view;
    private FrameLayout touchInterceptor;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.profile_fragment, container, false);
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listScores = getActivity().findViewById(R.id.TopObjList);
        editButton = getActivity().findViewById(R.id.EditNameButton);
        name = getActivity().findViewById(R.id.TextViewName);
        topScore = getActivity().findViewById(R.id.topScoreText);
        editName = getActivity().findViewById(R.id.hiddenEditName);
        switcher = getActivity().findViewById(R.id.nameSwitcher);
        touchInterceptor = getActivity().findViewById(R.id.touchInterceptor);

        name.setText(AppState.get().getNickName());
        editName.setText(AppState.get().getNickName());
        topScore.setText(AppState.get().getTopScore().toString());

        List<Pair<String, String>> arr2 = AppState.get().getListOfObjectsFound();
        ObjectsAdapter l2Adapter = new ObjectsAdapter(getActivity(), R.layout.two_column_item_list, arr2);
        listScores.setAdapter(l2Adapter);

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
                name.setText(editName.getText());
                switcher.showPrevious();
                hideKeyboardFrom(view);
            } else {
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

    public void hideKeyboardFrom(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void appearKeyboardFrom() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
}
