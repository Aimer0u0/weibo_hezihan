package com.example.weibo_hezihan;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private Fragment recommendFragment;
    private Fragment mineFragment;
    private Fragment activeFragment;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "activeFragment", activeFragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (savedInstanceState != null) {
            recommendFragment = getSupportFragmentManager().getFragment(savedInstanceState, "recommendFragment");
            mineFragment = getSupportFragmentManager().getFragment(savedInstanceState, "mineFragment");
            activeFragment = getSupportFragmentManager().getFragment(savedInstanceState, "activeFragment");
        } else {
            recommendFragment = new RecommendFragment();
            mineFragment = new MineFragment();
            activeFragment = recommendFragment;
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, recommendFragment, "recommend")
                    .commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(this::handleNavigationItemSelected);
    }

    @SuppressLint("NonConstantResourceId")
    private boolean handleNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.nav_recommend:
                fragment = recommendFragment;
                break;
            case R.id.nav_mine:
                fragment = mineFragment;
                break;
        }
        return switchFragment(fragment);
    }

    private boolean switchFragment(Fragment fragment) {
        if (fragment != null && fragment != activeFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (!fragment.isAdded()) {
                transaction.add(R.id.fragment_container, fragment, fragment == recommendFragment ? "recommend" : "mine");
            }
            transaction.hide(activeFragment).show(fragment).commitAllowingStateLoss();
            activeFragment = fragment;
            return true;
        }
        return false;
    }
}
