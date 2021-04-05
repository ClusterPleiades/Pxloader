package com.pleiades.pleione.pixivdownloader.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_USER;
import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_WORK;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_DATE;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_MONTH;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_VERSION_CODE;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_YEAR;
import static com.pleiades.pleione.pixivdownloader.Config.PREFS;
import static com.pleiades.pleione.pixivdownloader.Config.SETTING_PREFS;
import static com.pleiades.pleione.pixivdownloader.Variable.isLoggedIn;
import static com.pleiades.pleione.pixivdownloader.Variable.isNewbie;
import static com.pleiades.pleione.pixivdownloader.Variable.sentId;
import static com.pleiades.pleione.pixivdownloader.Variable.sentType;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize is newbie
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // initialize version code
            PackageInfo packageInfo = getPackageManager().getPackageInfo(this.getPackageName(), 0);
            int versionCode = packageInfo.versionCode;
            int prefsVersionCode = prefs.getInt(KEY_VERSION_CODE, 1);
            isNewbie = versionCode > prefsVersionCode;

            // TODO check
            if (isNewbie) {
                // apply update day, version code
                Calendar today = Calendar.getInstance();
                editor.putInt(KEY_YEAR, today.get(Calendar.YEAR));
                editor.putInt(KEY_MONTH, today.get(Calendar.MONTH));
                editor.putInt(KEY_DATE, today.get(Calendar.DATE));
                editor.putInt(KEY_VERSION_CODE, versionCode);
                editor.apply();

                // ~ 77 (4.6.1)
                if (prefsVersionCode <= 76) {
                    // clear setting prefs
                    SharedPreferences settingPrefs = getSharedPreferences(SETTING_PREFS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor settingEditor = settingPrefs.edit();
                    settingEditor.clear();
                    settingEditor.apply();
                }
            }
        } catch (Exception e) {
            // ignore exception block
        }

        // initialize sent type, id
        Intent sentIntent = getIntent();
        sentId = null;
        if (Intent.ACTION_SEND.equals(sentIntent.getAction())) {
            // example : title | nickname #pixiv  https://www.pixiv.net/artworks/id
            // example : nickname https://www.pixiv.net/users/id

            String extraString = sentIntent.getStringExtra(android.content.Intent.EXTRA_TEXT);
            if (extraString != null) {
                String[] splitExtraStrings = extraString.split("\\s");
                String coreExtraString = splitExtraStrings[splitExtraStrings.length - 1];

                if (coreExtraString.contains("https://www.pixiv.net/")) {
                    // case work id
                    if (extraString.contains("artworks")) {
                        splitExtraStrings = extraString.split("artworks/");
                        if (splitExtraStrings.length > 1) {
                            sentType = DOWNLOAD_TYPE_WORK;
                            sentId = splitExtraStrings[1];
                        }
                    }
                    // case user id
                    else if (extraString.contains("users")) {
                        splitExtraStrings = extraString.split("users/");
                        if (splitExtraStrings.length > 1) {
                            sentType = DOWNLOAD_TYPE_USER;
                            sentId = splitExtraStrings[1];
                        }
                    }
                }
            }
        }

        // initialize intent
        Intent intent;
        if (isLoggedIn)
            intent = new Intent(this, MainActivity.class);
        else
            intent = new Intent(this, LoginActivity.class);

        // start activity
        startActivity(intent);
        finish();
    }
}
