package org.redstudios.objecthunt;

import android.os.Bundle;

import org.redstudios.objecthunt.utils.NavigationHost;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements NavigationHost {

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
    }

    /**
     * Navigate to the given fragment.
     *
     * @param fragment       Fragment to navigate to.
     * @param addToBackstack Whether or not the current fragment should be added to the backstack.
     */
    @Override
    public void navigateTo(Fragment fragment, boolean addToBackstack) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment);

        if (addToBackstack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }
}

