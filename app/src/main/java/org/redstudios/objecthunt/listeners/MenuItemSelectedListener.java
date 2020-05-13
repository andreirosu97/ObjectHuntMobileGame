package org.redstudios.objecthunt.listeners;


import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.redstudios.objecthunt.MainActivity;
import org.redstudios.objecthunt.R;
import org.redstudios.objecthunt.mainactivity_fragments.GameModeSelectFragment;
import org.redstudios.objecthunt.mainactivity_fragments.LeaderboardFragment;
import org.redstudios.objecthunt.mainactivity_fragments.ProfileFragment;

import androidx.annotation.NonNull;

public class MenuItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {

    public MenuItemSelectedListener(MainActivity caller_) {
        caller = caller_;
    }

    private MainActivity caller;
    private int lastItemSelected = -1;

    private int opp_enter_anim = 0;
    private int opp_exit_anim = 0;

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (lastItemSelected == item.getItemId()) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.nav_profile:
                opp_enter_anim = R.anim.enter_to_right;
                opp_exit_anim = R.anim.exit_to_left;
                caller.navigateTo(new ProfileFragment(), (String) item.getTitle(), R.anim.enter_to_left, R.anim.exit_to_right);
                break;
            case R.id.nav_home:
                caller.navigateTo(new GameModeSelectFragment(), (String) item.getTitle(), opp_enter_anim, opp_exit_anim);
                break;
            case R.id.nav_leader:
                opp_enter_anim = R.anim.enter_to_left;
                opp_exit_anim = R.anim.exit_to_right;
                caller.navigateTo(new LeaderboardFragment(), (String) item.getTitle(), R.anim.enter_to_right, R.anim.exit_to_left);
                break;
        }
        lastItemSelected = item.getItemId();
        return true;
    }
}
