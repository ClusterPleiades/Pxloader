package com.pleiades.pleione.pixivdownloader.ui.activity;

import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_USER;
import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_WORK;
import static com.pleiades.pleione.pixivdownloader.Config.EXTRA_NAME_SENT_ID;
import static com.pleiades.pleione.pixivdownloader.Variable.sentId;
import static com.pleiades.pleione.pixivdownloader.Variable.sentType;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;
import com.pleiades.pleione.pixivdownloader.R;
import com.pleiades.pleione.pixivdownloader.ui.activity.download.UserActivity;
import com.pleiades.pleione.pixivdownloader.ui.activity.download.WorkActivity;
import com.pleiades.pleione.pixivdownloader.ui.fragment.ArchiveFragment;
import com.pleiades.pleione.pixivdownloader.ui.fragment.DownloadFragment;

public class MainActivity extends AppCompatActivity {
    private Activity activity;
    private int tabPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize activity
        activity = MainActivity.this;

        // initialize appbar
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // initialize tab layout
        TabLayout tabLayout = findViewById(R.id.main_tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabPosition = tab.getPosition();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_fragment, (tabPosition == 0) ? DownloadFragment.newInstance() : ArchiveFragment.newInstance());
                fragmentTransaction.commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (sentId != null) {
            Intent intent = null;

            if (sentType == DOWNLOAD_TYPE_USER)
                intent = new Intent(activity, UserActivity.class);
            else if (sentType == DOWNLOAD_TYPE_WORK)
                intent = new Intent(activity, WorkActivity.class);

            if (intent != null) {
                intent.putExtra(EXTRA_NAME_SENT_ID, sentId);
                sentId = null;
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.setting) {
            Intent intent = new Intent(activity, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (tabPosition == 0) {
            super.onBackPressed();
        } else {
            Fragment mainFragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
            if (mainFragment != null) {
                ArchiveFragment fragment = (ArchiveFragment) mainFragment;
                if (!fragment.onBackPressed())
                    super.onBackPressed();
            }
        }
    }
}