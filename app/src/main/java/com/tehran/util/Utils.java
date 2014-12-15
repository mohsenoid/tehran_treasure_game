package com.tehran.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Display;
import android.widget.TextView;


public class Utils {
    public static final String P_REGISTERED = "isRegistere";
    public static final String P_FIRST_RUN = "isFirstRun";
    public final static String P_FNAME = "fname";
    public final static String P_LNAME = "lname";
    public final static String P_MELLICODE = "melliCode";
    public final static String P_EMAIL = "email";
    public final static String P_BIRTH_YEAR = "birthYear";
    public final static String P_SEX = "sex";

    public static void exit() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static void exit(Activity activity) {
        exit((Context) activity);
        System.exit(0);

    }


    public static void exit(Context context) {
        try {
            Activity activity = (Activity) context;
            activity.finish();
            // System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkMelliCode(String melliCode) {
        try {
            melliCode = melliCode.replaceAll("-", "");
            if (melliCode.length() == 10) {
                if (melliCode.equals("0000000000")
                        || melliCode.equals("1111111111")
                        || melliCode.equals("2222222222")
                        || melliCode.equals("3333333333")
                        || melliCode.equals("4444444444")
                        || melliCode.equals("5555555555")
                        || melliCode.equals("6666666666")
                        || melliCode.equals("7777777777")
                        || melliCode.equals("8888888888")
                        || melliCode.equals("9999999999"))
                    return false;

                int c = Integer.valueOf(melliCode.charAt(9) + "");
                int n = Integer.valueOf(melliCode.charAt(0) + "") * 10
                        + Integer.valueOf(melliCode.charAt(1) + "") * 9
                        + Integer.valueOf(melliCode.charAt(2) + "") * 8
                        + Integer.valueOf(melliCode.charAt(3) + "") * 7
                        + Integer.valueOf(melliCode.charAt(4) + "") * 6
                        + Integer.valueOf(melliCode.charAt(5) + "") * 5
                        + Integer.valueOf(melliCode.charAt(6) + "") * 4
                        + Integer.valueOf(melliCode.charAt(7) + "") * 3
                        + Integer.valueOf(melliCode.charAt(8) + "") * 2;
                int r = n - (n / 11) * 11;

                if ((r == 0 && r == c) || (r == 1 && c == 1)
                        || (r > 1 && c == 11 - r))
                    return true;
                else
                    return false;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isFirstRun(Context context) {
        boolean firstRun = PreferenceManager.getDefaultSharedPreferences(
                context).getBoolean(P_FIRST_RUN, true);
        return firstRun;
    }

    public static void setFirstRun(Context context, boolean value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
                .edit();
        editor.putBoolean(P_FIRST_RUN, value);
        editor.commit();
    }

    public static void playSound(Context context, int rawID) {
        MediaPlayer mp = MediaPlayer.create(context, rawID);
        mp.start();
    }

    public static String getAndroidVersion() {
        // Android version
        return Build.VERSION.RELEASE;
    }

    public static int getAndroidVersionInt() {
        // Android version
        return Build.VERSION.SDK_INT;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static int getDisplayWidth(Context context) {
        try {
            Activity activity = (Activity) context;
            if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) < 13) {
                Display display = activity.getWindowManager()
                        .getDefaultDisplay();
                return display.getWidth();
            } else {
                Display display = activity.getWindowManager()
                        .getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                return size.x;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static int getDisplayHeight(Context context) {
        try {
            Activity activity = (Activity) context;
            if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) < 13) {
                Display display = activity.getWindowManager()
                        .getDefaultDisplay();
                return display.getHeight();
            } else {
                Display display = activity.getWindowManager()
                        .getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                return size.y;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Deprecated
    public static void setupFont(Context context, int id) {
        setupFont(context, FontName.Davat, id);
    }

    public static void setupFont(Context context, FontName fontName, int id) {
        try {
            Activity activity = (Activity) context;

            TextView tv = (TextView) activity.findViewById(id);

            setupFont(context, fontName, tv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setupFont(Context context, TextView tv) {
        setupFont(context, FontName.Davat, tv);
    }

    public static void setupFont(Context context, FontName fontName, TextView tv) {
        Typeface tf;
        switch (fontName) {
            case Davat:
                tf = Typeface.createFromAsset(context.getAssets(),
                        "fonts/BDavat.ttf");
                break;
            case Titr:
                tf = Typeface.createFromAsset(context.getAssets(),
                        "fonts/BTitrBd.ttf");
                break;
            case Zar:
                tf = Typeface
                        .createFromAsset(context.getAssets(), "fonts/BZar.ttf");
                break;
            case Traffic:
                tf = Typeface.createFromAsset(context.getAssets(),
                        "fonts/BTraffic.ttf");
                break;
            case Yagut:
                tf = Typeface.createFromAsset(context.getAssets(),
                        "fonts/BYagut.ttf");
                break;
            case AdobeArabic:
                tf = Typeface.createFromAsset(context.getAssets(),
                        "fonts/AdobeArabic.otf");
                break;
            case Koodak:
                tf = Typeface.createFromAsset(context.getAssets(),
                        "fonts/BKoodak.ttf");
                break;
            case Yekan:
                tf = Typeface.createFromAsset(context.getAssets(),
                        "fonts/BYekan.ttf");
                break;
            default:
                tf = Typeface.createFromAsset(context.getAssets(),
                        "fonts/BDavat.ttf");
                break;
        }

        tv.setTypeface(tf);
    }

    public static String getApplicationVersionName(Context context)
            throws PackageManager.NameNotFoundException {
        // Application version
        PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                context.getPackageName(), 0);
        return pInfo.versionName;
    }

    public static int getApplicationVersionCode(Context context)
            throws PackageManager.NameNotFoundException {
        // Application version
        PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                context.getPackageName(), 0);
        return pInfo.versionCode;
    }

    public static String getAndroidID(Context context) {
        String m_szAndroidID = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return m_szAndroidID;
    }

    public static enum FontName {
        Davat, Titr, Zar, Traffic, Yagut, AdobeArabic, Koodak, Yekan
    }
}
