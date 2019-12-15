package org.redstudios.objecthunt;

import android.os.Bundle;
import android.view.MenuItem;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    int opp_enter_anim = 0;
    int opp_exit_anim = 0;
    String currentTag = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new GameModeSelectFragment())
                    .commit();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.nav_home:
                        if(currentTag != item.getTitle()) {
                            opp_enter_anim = R.anim.enter_to_right;
                            opp_exit_anim = R.anim.exit_to_left;
                            MainActivity.this.navigateTo(new ProfileFragment(), (String)item.getTitle(), R.anim.enter_to_left, R.anim.exit_to_right);
                        }
                        break;
                    case R.id.nav_favorites:
                        if(currentTag!=  item.getTitle()) {
                            MainActivity.this.navigateTo(new GameModeSelectFragment(), (String)item.getTitle(), opp_enter_anim, opp_exit_anim);
                        }
                        break;
                    case R.id.nav_search:
                        if(currentTag !=  item.getTitle()) {
                            opp_enter_anim = R.anim.enter_to_left;
                            opp_exit_anim = R.anim.exit_to_right;
                            MainActivity.this.navigateTo(new LeaderboardFragment(), (String)item.getTitle(), R.anim.enter_to_right, R.anim.exit_to_left);
                        }
                        break;
                }
                return true;
            }
        });
    }

    public void navigateTo(Fragment fragment, String tag, int enter_anim, int exit_anim) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(enter_anim, exit_anim,enter_anim, exit_anim);
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.commit();
        currentTag = tag;
    }
}

