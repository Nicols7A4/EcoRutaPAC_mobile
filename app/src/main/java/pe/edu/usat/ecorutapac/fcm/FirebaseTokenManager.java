package pe.edu.usat.ecorutapac.fcm;

import android.content.Context;
import android.content.SharedPreferences;

public class FirebaseTokenManager {

    private static final String PREF_NAME = "ecoruta_fcm_pref";
    private static final String KEY_TOKEN = "fcm_token";
    private static final String KEY_DEVICE_NAME = "device_name";

    public static void setFirebaseToken(Context context, String token, String deviceName) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_DEVICE_NAME, deviceName)
                .apply();
    }

    public static String getFirebaseToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_TOKEN, null);
    }

    public static String getDeviceName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_DEVICE_NAME, "Android Device");
    }

    public static void clear(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().clear().apply();
    }
}
