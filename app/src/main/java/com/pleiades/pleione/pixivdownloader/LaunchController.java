package com.pleiades.pleione.pixivdownloader;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

import com.pleiades.pleione.pixivdownloader.client.Client;
import com.pleiades.pleione.pixivdownloader.pixiv.FavoritedCount;
import com.pleiades.pleione.pixivdownloader.pixiv.User;
import com.pleiades.pleione.pixivdownloader.pixiv.Work;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static com.pleiades.pleione.pixivdownloader.Config.AGES;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_COMPLETE_COUNT;
import static com.pleiades.pleione.pixivdownloader.Config.PREFS;

public class LaunchController {
    public static boolean filterWorkBookmarks(Work work, int bookmarks) {
        FavoritedCount favoritedCount = work.getStats().getFavoritedCount();
        return favoritedCount.getPrivateCount() + favoritedCount.getPublicCount() >= bookmarks;
    }

    public static boolean filterWorkAge(Work work, String age) {
        if (age.equals(AGES[0])) // all
            return true;
        return age.equals(work.getAgeLimit()); // all-age, r18
    }

    public static boolean filterWorkDelete(Work work) {
        User user = work.getUser();
        return user.getId() != 0 || !user.getAccount().equals("") || !user.getName().equals("");
    }

    public static boolean filterWorkTags(Context context, Work work) {
        ArrayList<String> tagList = DeviceController.getTagListPrefs(context);

        if (tagList.size() == 0)
            return true;

        List<String> workTags = work.getTags();

        for (String tag : tagList) {
            boolean isContain = false;

            for (String workTag : workTags) {
                if (workTag.contains(tag)) {
                    isContain = true;
                    break;
                }
            }

            if (isContain)
                return false;
        }
        return true;
    }

    public static void completeClient(final Context context, Client client) {
        int totalDownloadCount = client.totalDownloadCount;
        boolean isCompleted = !client.isShutDown();

        // shut down client
        client.shutDownProcess();

        // case no works found, login error, aggregated rankings error
        if (isCompleted && totalDownloadCount == 0)
            return;

        // count complete
        if (isCompleted) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            int completeCount = prefs.getInt(KEY_COMPLETE_COUNT, 0);
            editor.putInt(KEY_COMPLETE_COUNT, completeCount + 1);
            editor.apply();
        }

        // push notification
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancelAll();

            DeviceController.pushNotification(context,
                    context.getString(isCompleted ? R.string.message_complete : R.string.message_stop),
                    context.getString(R.string.count) + " " + totalDownloadCount,
                    NotificationCompat.PRIORITY_HIGH);
        });
    }
}
