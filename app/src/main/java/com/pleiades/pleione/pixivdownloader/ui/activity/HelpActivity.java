package com.pleiades.pleione.pixivdownloader.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.pleiades.pleione.pixivdownloader.Converter;
import com.pleiades.pleione.pixivdownloader.R;
import com.pleiades.pleione.pixivdownloader.ui.FixedViewPager;
import com.pleiades.pleione.pixivdownloader.ui.fragment.HelpFragment;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_SEARCH;
import static com.pleiades.pleione.pixivdownloader.Config.EXTRA_NAME_DOWNLOAD_TYPE;
import static com.pleiades.pleione.pixivdownloader.Variable.helpImageResIdList;

public class HelpActivity extends AppCompatActivity {
    Context context;
    int downloadType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // initialize context
        context = HelpActivity.this;

        // initialize appbar
        View appbar = findViewById(R.id.help_appbar);
        Toolbar toolbar = appbar.findViewById(R.id.include_toolbar);
        setSupportActionBar(toolbar);

        // initialize help resource id list
        downloadType = getIntent().getIntExtra(EXTRA_NAME_DOWNLOAD_TYPE, DOWNLOAD_TYPE_SEARCH);
        Converter.initializeHelpImageResIdList(downloadType);

        // initialize actionbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // initialize pager
        final FixedViewPager viewPager = findViewById(R.id.help_pager);
        HelpPagerAdapter pagerAdapter = new HelpPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        // initialize dots indicator
        DotsIndicator dotsIndicator = findViewById(R.id.help_indicator);
        dotsIndicator.setViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class HelpPagerAdapter extends FragmentPagerAdapter {
        private long baseID = 0;

        HelpPagerAdapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return HelpFragment.newInstance(downloadType, position);
        }

        @Override
        public int getCount() {
            return helpImageResIdList.size();
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            // refresh all fragments when data set changed
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public long getItemId(int position) {
            // give an ID different from position when position has been changed
            return baseID + position;
        }

        // create a new ID for each position to force recreation of the fragment
        public void notifyChangeInPosition(int n) { // number of items which have been changed
            // shift the ID returned by getItemId outside the range of all previous fragments
            baseID = baseID + getCount() + n;
        }
    }
}