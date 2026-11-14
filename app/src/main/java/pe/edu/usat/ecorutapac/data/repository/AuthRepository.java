package pe.edu.usat.ecorutapac.data.repository;

import android.content.Context;

import pe.edu.usat.ecorutapac.data.remote.RetrofitClient;
import pe.edu.usat.ecorutapac.data.remote.api.ApiService;
import pe.edu.usat.ecorutapac.data.remote.dto.request.DispositivoRequest;
import pe.edu.usat.ecorutapac.data.remote.dto.request.LoginRequest;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ApiResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.LoginResponse;
import pe.edu.usat.ecorutapac.fcm.FirebaseTokenManager;
import pe.edu.usat.ecorutapac.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;

public class AuthRepository {

    private final ApiService apiService;
    private final SessionManager sessionManager;
    private final Context context;

    public AuthRepository(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = RetrofitClient.getService(this.context);
        this.sessionManager = new SessionManager(this.context);
    }

    public void login(String correo, String password, Callback<ApiResponse<LoginResponse>> callback) {
        LoginRequest request = new LoginRequest(correo, password);
        apiService.login(request).enqueue(callback);
    }

    public void registrarTokenFcm(String token, String deviceName, Callback<ApiResponse<Object>> callback) {
        DispositivoRequest request = new DispositivoRequest(token, deviceName);
        apiService.registrarTokenFcm(request).enqueue(callback);
    }

    public void guardarSesion(LoginResponse response) {
        sessionManager.guardarSesion(response);
        RetrofitClient.setToken(response.getToken());
    }

    public void cerrarSesion(Callback<Void> onClosed) {
        sessionManager.cerrarSesion();
        FirebaseTokenManager.clear(context);
        RetrofitClient.setToken(null);
        if (onClosed != null) {
            onClosed.onResponse(null, null);
        }
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
