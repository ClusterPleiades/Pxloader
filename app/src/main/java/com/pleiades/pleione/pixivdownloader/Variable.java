package com.pleiades.pleione.pixivdownloader;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.pleiades.pleione.pixivdownloader.ui.Art;

import java.util.ArrayList;

public class Variable {
    public static boolean isPermissionGranted;

    public static int sentType;
    public static String sentId;
    public static boolean isNewbie;

    public static boolean isLoggedIn;
    public static boolean isGuest;
    public static String refreshToken;
    public static String accessToken;
    public static String accessUserId;
    public static long refreshTime;

    public static byte[] backupBytes;
    public static String message1, message2;

    public static ArrayList<Art> artList;

    public static ArrayList<Integer> helpImageResIdList;
}

