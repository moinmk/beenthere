package com.example.beenthere;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.beenthere.discover.discoverFragment;
import com.example.beenthere.profile.profileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AllActivityContainer extends AppCompatActivity {
    BottomNavigationView bottomNavigationBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_activity_container);

        bottomNavigationBar=findViewById(R.id.bottomNavigationBar);
        loadFragment(new discoverFragment());//loding default fragment page

        bottomNavigationBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment=null;
                switch(menuItem.getItemId()){
                    case R.id.discover_icon:
                        fragment=new discoverFragment();
                        break;
                    case R.id.search_icon:
                        fragment=new searchFragment();
                        break;
                    case R.id.notification_icon:
                        fragment=new notificationFragment();
                        break;
                    case R.id.profile_icon:
                        fragment=new profileFragment();
                        break;
                }
                return loadFragment(fragment);
            }
        });
    }


    //============to load selected fragment in the container===============
    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container,fragment)
                    .commit();
            return true;
        }
        return false;
    }

    //disabling back press button
    @Override
    public void onBackPressed() {

    }
}
