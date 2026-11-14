package pe.edu.usat.ecorutapac.util;

import android.content.Context;
import android.content.SharedPreferences;

import pe.edu.usat.ecorutapac.data.remote.dto.response.LoginResponse;

public class SessionManager {

    private static final String PREF_NAME = "ecoruta_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USUARIO_ID = "usuario_id";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_CORREO = "correo";
    private static final String KEY_ROL = "rol";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void guardarSesion(LoginResponse login) {
        preferences.edit()
                .putString(KEY_TOKEN, login.getToken())
                .putInt(KEY_USUARIO_ID, login.getId())
                .putString(KEY_NOMBRE, login.getNombre())
                .putString(KEY_CORREO, login.getCorreo())
                .putString(KEY_ROL, login.getRol())
                .apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public int getUsuarioId() {
        return preferences.getInt(KEY_USUARIO_ID, 0);
    }

    public String getNombre() {
        return preferences.getString(KEY_NOMBRE, "");
    }

    public String getCorreo() {
        return preferences.getString(KEY_CORREO, "");
    }

    public String getRol() {
        return preferences.getString(KEY_ROL, "");
    }

    public boolean existeSesion() {
        return getToken() != null && !getToken().trim().isEmpty();
    }

    public void cerrarSesion() {
        preferences.edit().clear().apply();
    }
}
