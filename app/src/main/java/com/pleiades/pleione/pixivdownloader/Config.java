package com.pleiades.pleione.pixivdownloader;

import android.Manifest;

public class Config {
    public static final String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static final int REQUEST_CODE_PERMISSIONS = 416;
    public static final int REQUEST_CODE_DIRECTORY = 11;
    public static final int SPAN_COUNT = 3;

    public static final String EXTRA_NAME_SENT_ID = "sent_id";
    public static final String EXTRA_NAME_DOWNLOAD_TYPE = "download_type";
    public static final String EXTRA_NAME_POSITION = "position";
    public static final String PARCELABLE_KEY_SCROLL = "scroll";

    public static final String[] REFRESH_TOKENS = {
            // hidden
    };
    public static final int PAGE_START = 1;
    public static final int PAGE_NO_NEXT = -1;
    public static final String URI_LOGIN = "https://oauth.secure.pixiv.net/auth/token";
    public static final String URI_SEARCH = "https://public-api.secure.pixiv.net/v1/search/works.json"; // TODO update
    public static final String URI_RANKINGS = "https://public-api.secure.pixiv.net/v1/ranking/all"; // TODO update
    public static final String URI_FOLLOWING = "https://public-api.secure.pixiv.net/v1/me/following/works.json";
    public static final String URI_COLLECTION = "https://app-api.pixiv.net/v1/user/bookmarks/illust";
    public static final String URI_USER = "https://public-api.secure.pixiv.net/v1/users/{authorId}/works.json";
    public static final String URI_WORK = "https://public-api.secure.pixiv.net/v1/works/{illustId}.json";
    public static final String URI_PXLOADER = "https://play.google.com/store/apps/details?id=com.pleiades.pleione.pixivdownloader";

    public static final String[] PATH_PXLOADER = {"Pictures", "Pxloader"};
    public static final String RELATIVE_PATH_SEARCH = "Search";
    public static final String RELATIVE_PATH_RANKINGS = "Rankings";
    public static final String RELATIVE_PATH_FOLLOWING = "Following";
    public static final String RELATIVE_PATH_COLLECTION = "Collection";
    public static final String RELATIVE_PATH_USER = "User";
    public static final String RELATIVE_PATH_WORK = "Work";

    public static final int DOWNLOAD_TYPE_SEARCH = -1;
    public static final int DOWNLOAD_TYPE_RANKINGS = 0;
    public static final int DOWNLOAD_TYPE_FOLLOWING = 1;
    public static final int DOWNLOAD_TYPE_COLLECTION = 2;
    public static final int DOWNLOAD_TYPE_USER = 3;
    public static final int DOWNLOAD_TYPE_WORK = 4;
    public static final int COUNT_DOWNLOAD_TYPE = 6;
    public static final int DOWNLOAD_TYPE_RATING = 5;

    public static final String[] ORDERS = {"desc", "asc", "popular"};
    public static final String[] AGES = {"all", "all-age", "r18"};
    public static final String[] AGGREGATION_MODES = {"daily", "weekly", "monthly", "male", "female", "rookie", "original", "daily_r18", "weekly_r18", "male_r18", "female_r18", "r18g"};
    public static final String[] PUBLICITIES = {"public", "private"};

    public static final int SETTING_TYPE_CREATE_SUB_DIRECTORY = 0;
    public static final int SETTING_TYPE_DIRECTORY_PATH = 1;
    public static final int SETTING_TYPE_FILE_NAME_FORMAT = 2;
    public static final int SETTING_TYPE_TAGS_TO_EXCLUDE = 3;
    public static final int SETTING_TYPE_ABOUT = 4;
    public static final int SETTING_TYPE_DISCLAIMER = 5;
    public static final int SETTING_TYPE_REMOVE_ADS = 6;
    public static final int SETTING_TYPE_SHARE = 7;

    public static final int FORMAT_TYPE_USER_ID = 0;
    public static final int FORMAT_TYPE_USER_NAME = 1;
    public static final int FORMAT_TYPE_WORK_TIME = 2;
    public static final int FORMAT_TYPE_WORK_ID = 3;
    public static final int FORMAT_TYPE_WORK_TITLE = 4;
    public static final int FORMAT_TYPE_WORK_BOOKMARKS = 5;

    public static final String CHANNEL_ID = "channel";
    public static final String CHANNEL_ID_HIGH = "channel_high";
    public static final String CHANNEL_NAME = "channel_name";
    public static final String CHANNEL_NAME_HIGH = "channel_name_high";

    public static final String PREFS = "prefs";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_YEAR = "year";
    public static final String KEY_MONTH = "month";
    public static final String KEY_DATE = "date";
    public static final String KEY_VERSION_CODE = "userLastVersionCode";
    public static final String KEY_COMPLETE_COUNT = "complete_count";
    public static final String KEY_IS_CROWN = "crown";
    public static final String KEY_IS_RATED = "rated";

    public static final String SETTING_PREFS = "settingPrefs";
    public static final String KEY_CREATE_SUB_DIRECTORY = "create_sub_directory";
    public static final String KEY_CUSTOM_DIRECTORY_URI = "custom_directory_uri";
    public static final String KEY_SELECTED_FORMATS = "selected_formats";
    public static final String KEY_TAGS_TO_EXCLUDE = "tags_to_exclude";
}
