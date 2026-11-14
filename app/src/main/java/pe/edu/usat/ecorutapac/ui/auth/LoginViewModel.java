package pe.edu.usat.ecorutapac.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import pe.edu.usat.ecorutapac.data.remote.dto.response.ApiResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.LoginResponse;
import pe.edu.usat.ecorutapac.data.repository.AuthRepository;
import pe.edu.usat.ecorutapac.ui.common.UiState;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<UiState<LoginResponse>> loginState = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
    }

    public LiveData<UiState<LoginResponse>> getLoginState() {
        return loginState;
    }

    public void iniciarSesion(String correo, String password) {
        if (correo == null || correo.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            loginState.setValue(UiState.error("Por favor complete todos los campos"));
            return;
        }

        loginState.setValue(UiState.loading());

        authRepository.login(correo.trim(), password.trim(), new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<LoginResponse>> call, @NonNull Response<ApiResponse<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    LoginResponse loginData = response.body().getData();
                    authRepository.guardarSesion(loginData);
                    loginState.setValue(UiState.content(loginData, false));
                } else if (response.code() == 401) {
                    loginState.setValue(UiState.error("Credenciales incorrectas"));
                } else {
                    loginState.setValue(UiState.error("Error del servidor: HTTP " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<LoginResponse>> call, @NonNull Throwable t) {
                loginState.setValue(UiState.error("No se pudo conectar al servidor: " + t.getMessage()));
            }
        });
    }

    public void registrarTokenFcm(String token, String deviceName) {
        if (token != null && !token.isEmpty()) {
            authRepository.registrarTokenFcm(token, deviceName, null);
        }
    }

    public boolean existeSesion() {
        return authRepository.getSessionManager().existeSesion();
    }
}
