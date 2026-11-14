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
import retrofit2.Response;

public class AuthRepository {

    private final SessionManager sessionManager;
    private final Context context;

    public AuthRepository(Context context) {
        this.context = context.getApplicationContext();
        this.sessionManager = new SessionManager(this.context);
    }

    private ApiService getApiService() {
        return RetrofitClient.getService(context);
    }

    private static <T> Callback<T> wrapCallback(Callback<T> callback) {
        if (callback != null) {
            return callback;
        }
        return new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {}
            @Override
            public void onFailure(Call<T> call, Throwable t) {}
        };
    }

    public void login(String correo, String password, Callback<ApiResponse<LoginResponse>> callback) {
        LoginRequest request = new LoginRequest(correo, password);
        getApiService().login(request).enqueue(wrapCallback(callback));
    }

    public void registrarTokenFcm(String token, String deviceName, Callback<ApiResponse<Object>> callback) {
        DispositivoRequest request = new DispositivoRequest(token, deviceName);
        getApiService().registrarTokenFcm(request).enqueue(wrapCallback(callback));
    }

    public void obtenerPerfil(Callback<ApiResponse<LoginResponse>> callback) {
        getApiService().obtenerPerfil().enqueue(wrapCallback(callback));
    }

    public void guardarSesion(LoginResponse response) {
        sessionManager.guardarSesion(response);
        RetrofitClient.setToken(response.getToken());
    }

    public void cerrarSesion(Runnable onClosed) {
        sessionManager.cerrarSesion();
        FirebaseTokenManager.clear(context);
        RetrofitClient.setToken(null);
        if (onClosed != null) {
            onClosed.run();
        }
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
