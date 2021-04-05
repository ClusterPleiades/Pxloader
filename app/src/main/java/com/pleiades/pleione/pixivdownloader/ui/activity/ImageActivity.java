package com.pleiades.pleione.pixivdownloader.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.pleiades.pleione.pixivdownloader.R;
import com.pleiades.pleione.pixivdownloader.ui.FixedViewPager;
import com.pleiades.pleione.pixivdownloader.ui.fragment.ImageFragment;

import java.io.File;

import static com.pleiades.pleione.pixivdownloader.Config.EXTRA_NAME_POSITION;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_CUSTOM_DIRECTORY_URI;
import static com.pleiades.pleione.pixivdownloader.Config.SETTING_PREFS;
import static com.pleiades.pleione.pixivdownloader.Variable.artList;

public class ImageActivity extends AppCompatActivity {
    private int position;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        // initialize context
        context = ImageActivity.this;

        // set status color
        getWindow().setStatusBarColor(ContextCompat.getColor(context, R.color.color_signature_dark_gray));

        // initialize appbar
        View appbar = findViewById(R.id.image_appbar);
        Toolbar toolbar = appbar.findViewById(R.id.image_toolbar);
        setSupportActionBar(toolbar);

        // initialize position
        position = getIntent().getIntExtra(EXTRA_NAME_POSITION, 0);

        // initialize actionbar
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(artList.get(position).name);
        }

        // initialize pager
        final FixedViewPager viewPager = findViewById(R.id.image_pager);
        ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(position);
        viewPager.addOnPageChangeListener(new FixedViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int selectedPosition) {
                position = selectedPosition;
                if (actionBar != null)
                    actionBar.setTitle(artList.get(position).name);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
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
        } else if (id == R.id.action_share){
            // initialize custom path string
            String customUriString = getSharedPreferences(SETTING_PREFS, Context.MODE_PRIVATE).getString(KEY_CUSTOM_DIRECTORY_URI, null);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("image/*");

            if (customUriString == null) {
                String pathName = artList.get(position).uri.getPath();
                if (pathName != null) {
                    File file = new File(pathName);
                    Uri uri = FileProvider.getUriForFile(ImageActivity.this, getPackageName() + ".fileprovider", file);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(intent);
                }
            } else {
                intent.putExtra(Intent.EXTRA_STREAM, artList.get(position).uri);
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private static class ImagePagerAdapter extends FragmentPagerAdapter {
        private long baseID = 0;

        ImagePagerAdapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return ImageFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return artList.size();
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