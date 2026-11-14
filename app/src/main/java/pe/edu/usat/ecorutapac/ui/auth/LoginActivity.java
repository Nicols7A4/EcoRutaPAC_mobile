package pe.edu.usat.ecorutapac.ui.auth;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.messaging.FirebaseMessaging;

import pe.edu.usat.ecorutapac.data.remote.RetrofitClient;
import pe.edu.usat.ecorutapac.databinding.ActivityLoginBinding;
import pe.edu.usat.ecorutapac.fcm.FirebaseTokenManager;
import pe.edu.usat.ecorutapac.ui.common.UiState;
import pe.edu.usat.ecorutapac.ui.main.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Si ya hay sesión activa, ir directo al MainActivity
        if (viewModel.existeSesion()) {
            irAMainActivity();
            finish();
            return;
        }

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getLoginState().observe(this, state -> {
            if (state == null) return;

            if (state.getEstado() == UiState.Estado.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnLogin.setEnabled(false);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
            }

            if (state.getEstado() == UiState.Estado.CONTENT) {
                Toast.makeText(this, "Bienvenido " + state.getData().getNombre() + " (" + state.getData().getRol() + ")", Toast.LENGTH_SHORT).show();
                sincronizarFcmToken();
                irAMainActivity();
                finish();
            } else if (state.getEstado() == UiState.Estado.ERROR) {
                Toast.makeText(this, state.getMensaje(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            String pass = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
            String serverUrl = binding.etServerUrl.getText() != null ? binding.etServerUrl.getText().toString().trim() : "";

            if (!TextUtils.isEmpty(serverUrl)) {
                RetrofitClient.setBaseUrl(serverUrl);
            }

            viewModel.iniciarSesion(email, pass);
        });

        // Botones de acceso rápido para facilitar la demostración de la rúbrica USAT
        binding.btnQuickSupervisor.setOnClickListener(v -> {
            binding.etEmail.setText("supervisor@ecoruta.pe");
            binding.etPassword.setText("Supervisor123");
        });

        binding.btnQuickOperario.setOnClickListener(v -> {
            binding.etEmail.setText("operario@ecoruta.pe");
            binding.etPassword.setText("Operario123");
        });

        binding.btnQuickFiscalizador.setOnClickListener(v -> {
            binding.etEmail.setText("fiscalizador@ecoruta.pe");
            binding.etPassword.setText("Fiscalizador123");
        });
    }

    private void sincronizarFcmToken() {
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String token = task.getResult();
                    String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
                    FirebaseTokenManager.setFirebaseToken(this, token, deviceName);
                    viewModel.registrarTokenFcm(token, deviceName);
                }
            });
        } catch (Exception ignored) {}
    }

    private void irAMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
