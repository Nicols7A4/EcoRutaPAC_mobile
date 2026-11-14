package pe.edu.usat.ecorutapac.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import pe.edu.usat.ecorutapac.data.remote.dto.response.ApiResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.LoginResponse;
import pe.edu.usat.ecorutapac.data.repository.AuthRepository;
import pe.edu.usat.ecorutapac.databinding.FragmentPerfilBinding;
import pe.edu.usat.ecorutapac.fcm.FirebaseTokenManager;
import pe.edu.usat.ecorutapac.ui.auth.LoginActivity;
import pe.edu.usat.ecorutapac.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private FragmentPerfilBinding binding;
    private SessionManager sessionManager;
    private AuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        authRepository = new AuthRepository(requireContext());

        // Mostrar datos iniciales cacheados localmente
        actualizarUI(sessionManager.getNombre(), sessionManager.getRol(), sessionManager.getCorreo());

        // Consultar datos actualizados del perfil desde el servidor (GET /api/usuario/perfil)
        authRepository.obtenerPerfil(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<LoginResponse>> call, @NonNull Response<ApiResponse<LoginResponse>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    LoginResponse perfil = response.body().getData();
                    actualizarUI(perfil.getNombre(), perfil.getRol(), perfil.getCorreo());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<LoginResponse>> call, @NonNull Throwable t) {
                // Si falla la red, mantenemos los datos cacheados
            }
        });

        String fcmToken = FirebaseTokenManager.getFirebaseToken(requireContext());
        if (fcmToken != null) {
            binding.tvFcmToken.setText("FCM Token: Conectado (" + fcmToken.substring(0, Math.min(20, fcmToken.length())) + "...)");
        } else {
            binding.tvFcmToken.setText("FCM Token: Inicializando con Firebase");
        }

        binding.btnCerrarSesion.setOnClickListener(v -> {
            authRepository.cerrarSesion(() -> {
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
        });
    }

    private void actualizarUI(String nombre, String rol, String correo) {
        if (binding != null) {
            binding.tvNombreUsuario.setText(nombre != null && !nombre.isEmpty() ? nombre : "Usuario EcoRuta");
            binding.tvRolUsuario.setText("Rol: " + (rol != null ? rol : ""));
            binding.tvCorreoUsuario.setText(correo != null ? correo : "");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
