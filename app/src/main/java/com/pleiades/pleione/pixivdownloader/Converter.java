package com.pleiades.pleione.pixivdownloader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Environment;

import androidx.documentfile.provider.DocumentFile;

import com.pleiades.pleione.pixivdownloader.pixiv.FavoritedCount;
import com.pleiades.pleione.pixivdownloader.pixiv.Work;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.ZipInputStream;

import static com.pleiades.pleione.pixivdownloader.Config.AGES;
import static com.pleiades.pleione.pixivdownloader.Config.AGGREGATION_MODES;
import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_COLLECTION;
import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_FOLLOWING;
import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_RANKINGS;
import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_RATING;
import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_SEARCH;
import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_USER;
import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_WORK;
import static com.pleiades.pleione.pixivdownloader.Config.FORMAT_TYPE_USER_ID;
import static com.pleiades.pleione.pixivdownloader.Config.FORMAT_TYPE_USER_NAME;
import static com.pleiades.pleione.pixivdownloader.Config.FORMAT_TYPE_WORK_BOOKMARKS;
import static com.pleiades.pleione.pixivdownloader.Config.FORMAT_TYPE_WORK_TIME;
import static com.pleiades.pleione.pixivdownloader.Config.FORMAT_TYPE_WORK_ID;
import static com.pleiades.pleione.pixivdownloader.Config.FORMAT_TYPE_WORK_TITLE;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_CREATE_SUB_DIRECTORY;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_CUSTOM_DIRECTORY_URI;
import static com.pleiades.pleione.pixivdownloader.Config.ORDERS;
import static com.pleiades.pleione.pixivdownloader.Config.PATH_PXLOADER;
import static com.pleiades.pleione.pixivdownloader.Config.PUBLICITIES;
import static com.pleiades.pleione.pixivdownloader.Config.SETTING_PREFS;
import static com.pleiades.pleione.pixivdownloader.Variable.helpImageResIdList;

public class Converter {
    // download type to label
    public static int getDownloadLabelResId(int downloadType) {
        switch (downloadType) {
            case DOWNLOAD_TYPE_SEARCH:
                return R.string.label_search;
            case DOWNLOAD_TYPE_RANKINGS:
                return R.string.label_rankings;
            case DOWNLOAD_TYPE_FOLLOWING:
                return R.string.label_following;
            case DOWNLOAD_TYPE_COLLECTION:
                return R.string.label_collection;
            case DOWNLOAD_TYPE_USER:
                return R.string.label_user;
            case DOWNLOAD_TYPE_WORK:
                return R.string.label_work;
            case DOWNLOAD_TYPE_RATING:
                return R.string.label_rating;
            default:
                return 0;
        }
    }

    // download type to help message
    public static String getHelpMessage(Context context, int downloadType, int position) {
        String[] helpMessages;
        if (downloadType == DOWNLOAD_TYPE_SEARCH)
            helpMessages = context.getResources().getStringArray(R.array.help_message_search);
        else if (downloadType == DOWNLOAD_TYPE_USER)
            helpMessages = context.getResources().getStringArray(R.array.help_message_user);
        else if (downloadType == DOWNLOAD_TYPE_WORK)
            helpMessages = context.getResources().getStringArray(R.array.help_message_work);
        else
            return null;
        return helpMessages[position];
    }

    // download type to help resource id list
    public static void initializeHelpImageResIdList(int downloadType) {
        helpImageResIdList = new ArrayList<>();
        switch (downloadType) {
            case DOWNLOAD_TYPE_SEARCH:
                helpImageResIdList.add(R.drawable.image_search_0);
                helpImageResIdList.add(R.drawable.image_search_1);
                helpImageResIdList.add(R.drawable.image_search_2);
                helpImageResIdList.add(R.drawable.image_search_3);
                helpImageResIdList.add(R.drawable.image_search_4);
                break;
            case DOWNLOAD_TYPE_USER:
                helpImageResIdList.add(R.drawable.image_user_0);
                helpImageResIdList.add(R.drawable.image_user_1);
                helpImageResIdList.add(R.drawable.image_user_2);
                helpImageResIdList.add(R.drawable.image_user_3);
                break;
            case DOWNLOAD_TYPE_WORK:
                helpImageResIdList.add(R.drawable.image_work_0);
                helpImageResIdList.add(R.drawable.image_work_1);
                helpImageResIdList.add(R.drawable.image_work_2);
                break;
        }
    }

    // format type to format
    public static String getFormat(Work work, int formatType) {
        switch (formatType) {
            case FORMAT_TYPE_USER_ID:
                return Integer.toString(work.getUser().getId());
            case FORMAT_TYPE_USER_NAME:
                return work.getUser().getName();
            case FORMAT_TYPE_WORK_TIME:
                // 2019-08-17 17_13_24
                String time = work.getCreatedTime();
                String date = time.replaceAll("[^0-9]", "");
                return date.substring(0, 8);
            case FORMAT_TYPE_WORK_ID:
                return Integer.toString(work.getId());
            case FORMAT_TYPE_WORK_TITLE:
                return work.getTitle();
            case FORMAT_TYPE_WORK_BOOKMARKS:
                FavoritedCount favoritedCount = work.getStats().getFavoritedCount();
                int bookmarks = favoritedCount.getPublicCount() + favoritedCount.getPrivateCount();
                return Integer.toString(bookmarks);
            default:
                return "";
        }
    }

    // input to attr
    public static String getKeyword(String inputKeyword) {
        if (inputKeyword.equals(""))
            return null;
        return inputKeyword;
    }

    // input to attr
    public static int getBookmarks(String inputBookmarks) {
        try {
            return Integer.parseInt(inputBookmarks);
        } catch (Exception e) {
            return 0;
        }
    }

    // input to attr
    public static String getOrder(Context context, String inputOrder) {
        String[] validInputOrders = context.getResources().getStringArray(R.array.input_orders);
        if (inputOrder.equals(validInputOrders[0])) // desc
            return ORDERS[0];
        if (inputOrder.equals(validInputOrders[1])) // asc
            return ORDERS[1];
        if (inputOrder.equals(ORDERS[2])) // popular
            return ORDERS[2];
        return ORDERS[0];
    }

    // input to attr
    public static String getAge(Context context, String inputAge) {
        String[] validInputAges = context.getResources().getStringArray(R.array.input_ages);
        if (inputAge.equals(validInputAges[0])) // all
            return AGES[0];
        if (inputAge.equals(validInputAges[1])) // all ages
            return AGES[1];
        if (inputAge.equals(validInputAges[2])) // R-18
            return AGES[2];
        return AGES[0];
    }

    // input to attr
    public static String getDate(String inputDate) {
        if (inputDate.equals(""))
            return null;
        return inputDate;
    }

    // input to attr
    public static String getAggregationMode(Context context, String inputAggregationMode) {
        String[] validAggregationModes = context.getResources().getStringArray(R.array.input_aggregation_modes);
        for (int i = 0; i < validAggregationModes.length; i++) {
            if (inputAggregationMode.equals(validAggregationModes[i]))
                return AGGREGATION_MODES[i];
        }
        return null;
    }

    // input to attr
    public static String getPublicity(Context context, String inputPublicity) {
        String[] validInputPublicityArray = context.getResources().getStringArray(R.array.input_publicities);
        if (inputPublicity.equals(validInputPublicityArray[0]))
            return PUBLICITIES[0];
        if (inputPublicity.equals(validInputPublicityArray[1]))
            return PUBLICITIES[1];
        return null;
    }

    // attr to relative path
    public static String getAggregationModeRelativePath(Context context, String aggregationMode) {
        for (int i = 0; i < AGGREGATION_MODES.length; i++) {
            if (aggregationMode.equals(AGGREGATION_MODES[i])) {
                Configuration configuration = context.getResources().getConfiguration();
                configuration = new Configuration(configuration);
                configuration.setLocale(new Locale("en"));
                Context localizedContext = context.createConfigurationContext(configuration);
                return localizedContext.getResources().getStringArray(R.array.input_aggregation_modes)[i];
            }
        }
        return null;
    }

    // attr to relative path
    public static String getPublicityRelativePath(Context context, String publicity) {
        for (int i = 0; i < PUBLICITIES.length; i++) {
            if (publicity.equals(PUBLICITIES[i])) {
                Configuration configuration = context.getResources().getConfiguration();
                configuration = new Configuration(configuration);
                configuration.setLocale(new Locale("en"));
                Context localizedContext = context.createConfigurationContext(configuration);
                return localizedContext.getResources().getStringArray(R.array.input_publicities)[i];
            }
        }
        return null;
    }

    // work to file name
    public static String getFileName(Context context, Work work, int index) {
        StringBuilder fileNameStringBuilder;

        // initialize file name string builder
        ArrayList<Integer> formatTypeList = DeviceController.getFormatListPrefs(context);
        if (formatTypeList == null)
            fileNameStringBuilder = new StringBuilder(Integer.toString(work.getId()));
        else {
            fileNameStringBuilder = new StringBuilder(Converter.getFormat(work, formatTypeList.remove(0)));
            for (Integer formatType : formatTypeList) {
                String contents = Converter.getFormat(work, formatType);
                fileNameStringBuilder.append("_").append(contents);
            }
        }

        // append index
        if (index != -1)
            fileNameStringBuilder.append("_").append(index);

        // append extension
        String workUrl;
        if (work.isUgoira())
            fileNameStringBuilder.append(".gif");
        else {
            if (work.isManga())
                workUrl = work.getMetadata().getPages().get(index - 1).getImageUrls().getLarge();
            else
                workUrl = work.getImageUrls().getLarge();
            String[] splitWorkUrl = workUrl.split("\\.");
            fileNameStringBuilder.append(".").append(splitWorkUrl[splitWorkUrl.length - 1]);
        }

        return fileNameStringBuilder.toString();
    }

    // relative paths to directory document file
    public static DocumentFile getDirectoryDocumentFile(Context context, String... relativePaths) {
        SharedPreferences settingPrefs = context.getSharedPreferences(SETTING_PREFS, Context.MODE_PRIVATE);
        DocumentFile directoryDocumentFile;

        // initialize directory document file
        String customDirectoryUri = settingPrefs.getString(KEY_CUSTOM_DIRECTORY_URI, null);
        if (customDirectoryUri == null) {
            File storageFile = Environment.getExternalStorageDirectory();
            directoryDocumentFile = DocumentFile.fromFile(storageFile);
            for (String path : PATH_PXLOADER) {
                if (directoryDocumentFile != null) {
                    if (directoryDocumentFile.findFile(path) == null)
                        directoryDocumentFile.createDirectory(path);
                    directoryDocumentFile = directoryDocumentFile.findFile(path);
                }
            }
        } else {
            directoryDocumentFile = DocumentFile.fromTreeUri(context, Uri.parse(customDirectoryUri));
        }

        // case create sub directory
        if (settingPrefs.getBoolean(KEY_CREATE_SUB_DIRECTORY, true)) {
            // set relative paths
            for (String path : relativePaths) {
                if (directoryDocumentFile != null) {
                    if (directoryDocumentFile.findFile(path) == null)
                        directoryDocumentFile.createDirectory(path);
                    directoryDocumentFile = directoryDocumentFile.findFile(path);
                }
            }
        }

        return directoryDocumentFile;
    }

    // zip bytes to bytes list
    public static ArrayList<byte[]> getFrameBytesList(byte[] zipBytes) {
        ArrayList<byte[]> frameBytesList = new ArrayList<>();
        byte[] buffer = new byte[2048];
        int read;

        try {
            // initialize zip input stream
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new ByteArrayInputStream(zipBytes)));

            // extract zip
            while (zipInputStream.getNextEntry() != null) {
                // save frame byte file
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                while ((read = zipInputStream.read(buffer)) > 0)
                    outputStream.write(buffer, 0, read);

                byte[] frameByteFile = outputStream.toByteArray();
                frameBytesList.add(frameByteFile);

                outputStream.flush();
                outputStream.close();
            }

            zipInputStream.close();

        } catch (Exception e) {
            // ignore exception block
        }

        return frameBytesList;
    }
}
