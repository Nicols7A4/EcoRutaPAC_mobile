package pe.edu.usat.ecorutapac.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import pe.edu.usat.ecorutapac.R;
import pe.edu.usat.ecorutapac.databinding.ActivityMainBinding;
import pe.edu.usat.ecorutapac.sync.SyncManager;
import pe.edu.usat.ecorutapac.ui.auth.LoginActivity;
import pe.edu.usat.ecorutapac.ui.rutas.RutasFragment;
import pe.edu.usat.ecorutapac.ui.rutas.RutasViewModel;
import pe.edu.usat.ecorutapac.ui.supervisor.ResumenSupervisionActivity;
import pe.edu.usat.ecorutapac.util.NetworkUtils;
import pe.edu.usat.ecorutapac.util.SessionManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SessionManager sessionManager;
    private RutasViewModel rutasViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.existeSesion()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        rutasViewModel = new ViewModelProvider(this).get(RutasViewModel.class);

        setupToolbar();
        setupBottomNavigation();
        setupOfflineBanner();

        // Cargar Fragmento Inicial
        if (savedInstanceState == null) {
            cambiarFragmento(new RutasFragment());
        }
    }

    private void setupToolbar() {
        binding.toolbar.setTitle("EcoRuta — " + sessionManager.getRol());
        binding.toolbar.setSubtitle(sessionManager.getNombre());
    }

    private void setupBottomNavigation() {
        String rol = sessionManager.getRol();

        // Si es Operario, ocultar la pestaña de supervisión si se desea, o abrir mapa/resumen
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_rutas) {
                cambiarFragmento(new RutasFragment());
                return true;
            } else if (itemId == R.id.nav_supervision) {
                if ("OPERARIO".equalsIgnoreCase(rol)) {
                    Toast.makeText(this, "El módulo de supervisión general es para Supervisor y Fiscalizador", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, ResumenSupervisionActivity.class));
                }
                return true;
            } else if (itemId == R.id.nav_perfil) {
                cambiarFragmento(new PerfilFragment());
                return true;
            }
            return false;
        });
    }

    private void setupOfflineBanner() {
        View bannerView = binding.bannerOffline.getRoot();
        TextView tvOfflineMsg = bannerView.findViewById(R.id.tvOfflineMessage);
        Button btnSincronizar = bannerView.findViewById(R.id.btnSincronizar);

        btnSincronizar.setOnClickListener(v -> {
            Toast.makeText(this, "Iniciando sincronización con WorkManager...", Toast.LENGTH_SHORT).show();
            SyncManager.encolarSincronizacion(this);
        });

        // Observar cantidad de operaciones pendientes en Room (Módulo 5 & Reto R1)
        rutasViewModel.getOperacionesPendientesCount().observe(this, pendientes -> {
            int totalPendientes = pendientes != null ? pendientes : 0;
            boolean sinRed = !NetworkUtils.hayConexion(this);

            if (sinRed || totalPendientes > 0) {
                bannerView.setVisibility(View.VISIBLE);
                if (sinRed) {
                    tvOfflineMsg.setText("Modo Offline Activo | " + totalPendientes + " operaciones pendientes");
                } else {
                    tvOfflineMsg.setText("Conexión restablecida | " + totalPendientes + " pendientes de sincronizar");
                }
            } else {
                bannerView.setVisibility(View.GONE);
            }
        });
    }

    private void cambiarFragmento(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Verificar estado de red para banner
        boolean sinRed = !NetworkUtils.hayConexion(this);
        if (sinRed) {
            binding.bannerOffline.getRoot().setVisibility(View.VISIBLE);
        }
    }
}
