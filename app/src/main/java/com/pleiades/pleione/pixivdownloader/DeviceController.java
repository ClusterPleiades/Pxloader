package com.pleiades.pleione.pixivdownloader;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WINDOW_SERVICE;
import static com.pleiades.pleione.pixivdownloader.Config.CHANNEL_ID;
import static com.pleiades.pleione.pixivdownloader.Config.CHANNEL_ID_HIGH;
import static com.pleiades.pleione.pixivdownloader.Config.CHANNEL_NAME;
import static com.pleiades.pleione.pixivdownloader.Config.CHANNEL_NAME_HIGH;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_SELECTED_FORMATS;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_TAGS_TO_EXCLUDE;
import static com.pleiades.pleione.pixivdownloader.Config.SETTING_PREFS;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.documentfile.provider.DocumentFile;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pleiades.pleione.pixivdownloader.pixiv.AndroidGIFEncoder;
import com.pleiades.pleione.pixivdownloader.pixiv.Frame;
import com.pleiades.pleione.pixivdownloader.pixiv.Work;
import com.pleiades.pleione.pixivdownloader.ui.Art;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DeviceController {
    public static int getWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
            return windowMetrics.getBounds().width();
        } else {
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return size.x;
        }
    }

    public static int getTotalMemory(Context context) {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        return (int) (memoryInfo.totalMem / 1048576L / 1024);
    }

    public static void pushNotification(Context context, String message, String by, int priority) {
        String channelId, channelName;
        int importance;

        // initialize channel attributes
        if (priority == NotificationCompat.PRIORITY_HIGH) {
            channelId = CHANNEL_ID_HIGH;
            channelName = CHANNEL_NAME_HIGH;
        } else {
            channelId = CHANNEL_ID;
            channelName = CHANNEL_NAME;
        }

        // initialize notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // set notification channel with importance for oreo or upper
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // initialize importance
            if (priority == NotificationCompat.PRIORITY_HIGH)
                importance = NotificationManager.IMPORTANCE_HIGH;
            else
                importance = NotificationManager.IMPORTANCE_LOW;

            // create notification channel
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        // set click listener
//        Intent intent = new Intent(context, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
//        .setContentIntent(pendingIntent)

        // initialize notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        builder
                .setDefaults(Notification.DEFAULT_SOUND)
                .setVibrate(new long[]{0L})
                .setAutoCancel(false)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setPriority(priority);

        if (by != null)
            builder.setSubText(by);

        // build notification
        notificationManager.notify(0, builder.build());
    }

    public static void showOptimizationDialog(Context context) {
        // initialize builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogBuilderTheme);
        builder.setMessage(R.string.message_ignore_optimization);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            context.startActivity(intent);
        });

        // show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // set dialog message attributes
        TextView messageTextView = dialog.findViewById(android.R.id.message);
        if (messageTextView != null) {
            messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text_size_default));
            messageTextView.setLineSpacing(0f, 1.2f);
        }
    }

    public static void readArtList(ArrayList<Art> artList, DocumentFile documentFile) {
        if (documentFile.isDirectory()) {
            for (DocumentFile subDocumentFile : documentFile.listFiles())
                readArtList(artList, subDocumentFile);
        } else if (documentFile.isFile()) {
            String fileName = documentFile.getName();
            if (fileName != null)
                if (fileName.endsWith(".png")
                        || fileName.endsWith(".jpg")
                        || fileName.endsWith("jpeg")
                        || fileName.endsWith("JPG")
                        || fileName.endsWith("JPEG")
                        || fileName.endsWith("PNG")
                        || fileName.endsWith(".gif")) {
                    Uri pathUri = documentFile.getUri();
                    long lastModified = documentFile.lastModified();
                    Art art = new Art(fileName, pathUri, lastModified);
                    artList.add(art);
                }
        }
    }

    public static void saveBytes(Context context, Work work, byte[] bytes, String fileName, String... relativePaths) {
        if (bytes == null)
            return;

        // initialize directory document file
        DocumentFile directoryDocumentFile = Converter.getDirectoryDocumentFile(context, relativePaths);
        if (directoryDocumentFile == null)
            return;

        // delete origin document file
        DocumentFile originDocumentFile = directoryDocumentFile.findFile(fileName);
        if (originDocumentFile != null)
            originDocumentFile.delete();

        // create save document file
        directoryDocumentFile.createFile("image/*", fileName);
        DocumentFile saveDocumentFile = directoryDocumentFile.findFile(fileName);
        if (saveDocumentFile == null)
            return;

        // save bytes
        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(saveDocumentFile.getUri());
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

            // case ugoira (bytes is zip)
            if (work.isUgoira()) {
                // initialize frame list
                ArrayList<byte[]> frameBytesList = Converter.getFrameBytesList(bytes);
                List<Frame> frameList = work.getMetadata().getFrames();

                // encode gif
                AndroidGIFEncoder gifEncoderAndroid = new AndroidGIFEncoder();
                gifEncoderAndroid.start(bufferedOutputStream);
                for (int i = 0; i < frameBytesList.size(); i++) {
                    gifEncoderAndroid.setDelay(frameList.get(i).getDelayMsec());
                    gifEncoderAndroid.addFrame(BitmapFactory.decodeByteArray(frameBytesList.get(i), 0, frameBytesList.get(i).length));
                }
                gifEncoderAndroid.finish();
            } else
                bufferedOutputStream.write(bytes);

            bufferedOutputStream.flush();
            bufferedOutputStream.close();
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }

            // scan media
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(saveDocumentFile.getUri());
            context.sendBroadcast(intent);
        } catch (Exception e) {
            // ignore exception block
        }
    }

    // prefs
    public static ArrayList<Integer> getFormatListPrefs(Context context) {
        SharedPreferences settingPrefs = context.getSharedPreferences(SETTING_PREFS, MODE_PRIVATE);
        String json = settingPrefs.getString(KEY_SELECTED_FORMATS, null);

        if (json == null)
            return null;
        else {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Integer>>() {
            }.getType();

            return gson.fromJson(json, type);
        }
    }

    public static ArrayList<String> getTagListPrefs(Context context) {
        SharedPreferences settingPrefs = context.getSharedPreferences(SETTING_PREFS, MODE_PRIVATE);
        String json = settingPrefs.getString(KEY_TAGS_TO_EXCLUDE, null);

        if (json == null)
            return new ArrayList<>();
        else {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();

            return gson.fromJson(json, type);
        }
    }

    // dp to px
//    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
}
