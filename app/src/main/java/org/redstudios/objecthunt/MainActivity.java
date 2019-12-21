package org.redstudios.objecthunt;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.redstudios.objecthunt.listeners.MenuItemSelectedListener;
import org.redstudios.objecthunt.mainactivity_fragments.GameModeSelectFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

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
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new MenuItemSelectedListener(this));
    }

    public void navigateTo(Fragment fragment, String tag, int enter_anim, int exit_anim) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(enter_anim, exit_anim, enter_anim, exit_anim);
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.commit();
    }
}


