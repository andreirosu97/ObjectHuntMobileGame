package org.redstudios.objecthunt.mainactivity_fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;

import org.redstudios.objecthunt.R;
import org.redstudios.objecthunt.model.AppState;
import org.redstudios.objecthunt.model.GameMode;
import org.redstudios.objecthunt.model.LeaderboardLVAdapter;
import org.redstudios.objecthunt.utils.CallbackableWithBoolean;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class LeaderboardFragment extends Fragment implements CallbackableWithBoolean {
    private final Integer ANIM_DURATION = 100;
    private ListView listUsers;
    private Button prevButton;
    private Button nextButton;
    private Button leaderButton;
    private TextView gameModeTitle;
    private ProgressBar progressBar;
    private List<GameMode> gameModes;
    private Integer currentGameMode;
    private ViewGroup header;
    private RelativeLayout ldbLay;
    final Handler handler = new Handler();
    AlphaAnimation fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);//fade from 0 to 1 alpha
    AlphaAnimation fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);//fade from 1 to 0 alpha
    private static final int RC_LEADERBOARD_UI = 9004;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.leaderboard_fragment, container, false);
        listUsers = view.findViewById(R.id.usersRank);
        progressBar = view.findViewById(R.id.progressBar_cyclic);
        prevButton = view.findViewById(R.id.prev_game_mode);
        leaderButton = view.findViewById(R.id.leader_button);
        nextButton = view.findViewById(R.id.next_game_mode);
        gameModeTitle = view.findViewById(R.id.game_mode_title);
        header = view.findViewById(R.id.leader_header);
        ldbLay = view.findViewById(R.id.ldb_rel_lay);
        gameModes = AppState.get().getGameModesList();

        fadeInAnimation.setDuration(ANIM_DURATION);
        fadeInAnimation.setFillAfter(true);
        fadeOutAnimation.setDuration(ANIM_DURATION);
        fadeOutAnimation.setFillAfter(true);

        prevButton.setOnClickListener((View v) -> {
            ldbLay.startAnimation(fadeOutAnimation);
            prevButton.setEnabled(false);
            handler.postDelayed(() -> {
                loadLeaderBoard(getPrevGameMode());
            }, 600);
        });

        nextButton.setOnClickListener((View v) -> {
            ldbLay.startAnimation(fadeOutAnimation);
            nextButton.setEnabled(false);
            handler.postDelayed(() -> {
                loadLeaderBoard(getNextGameMode());
            }, 600);
        });

        leaderButton.setOnClickListener((View v) -> showLeaderboard());

        header = (ViewGroup) inflater.inflate(R.layout.leader_board_header, listUsers, false);
        listUsers.addHeaderView(header);
        loadLeaderBoard(AppState.get().getRandomGameMode());
        return view;
    }

    private void loadLeaderBoard(GameMode gameMode) {
        if (getActivity() != null) {
            currentGameMode = gameModes.indexOf(gameMode);
            ldbLay.setVisibility(View.GONE);
            prevButton.setVisibility(View.GONE);
            leaderButton.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);
            listUsers.setVisibility(View.GONE);
            gameModeTitle.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            gameModeTitle.setText(gameMode.getGameModeName());
            progressBar.startAnimation(fadeInAnimation);
            handler.postDelayed(() -> {
                AppState.get().loadLeaderBoard(gameMode, this);
            }, ANIM_DURATION);
        }
    }

    @Override
    public void callback(Boolean completionStatus) {
        if (getActivity() != null) {
            LeaderboardLVAdapter objectsFoundAdapter = new LeaderboardLVAdapter(this.getActivity(), R.layout.leaderboard_row, AppState.get().getScores());
            listUsers.setAdapter(objectsFoundAdapter);
            progressBar.startAnimation(fadeOutAnimation);
            handler.postDelayed(() -> {
                progressBar.setVisibility(View.GONE);
                ldbLay.startAnimation(fadeInAnimation);
                ldbLay.setVisibility(View.VISIBLE);
                prevButton.setVisibility(View.VISIBLE);
                prevButton.setEnabled(true);
                nextButton.setEnabled(true);
                leaderButton.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.VISIBLE);
                listUsers.setVisibility(View.VISIBLE);
                gameModeTitle.setVisibility(View.VISIBLE);
            }, ANIM_DURATION);
        }
    }

    private GameMode getNextGameMode() {
        if (currentGameMode == gameModes.size() - 1) {
            currentGameMode = 0;
        } else {
            currentGameMode++;
        }
        return gameModes.get(currentGameMode);
    }

    private GameMode getPrevGameMode() {
        if (currentGameMode == 0) {
            currentGameMode = gameModes.size() - 1;
        } else {
            currentGameMode--;
        }
        return gameModes.get(currentGameMode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_LEADERBOARD_UI)
            leaderButton.setEnabled(true);
    }

    private void showLeaderboard() {
        leaderButton.setEnabled(false);
        AppState.get().getLeaderboardsClient()
                .getLeaderboardIntent(gameModes.get(currentGameMode).getLeaderboardId())
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_LEADERBOARD_UI);
                    }
                });
    }
}
