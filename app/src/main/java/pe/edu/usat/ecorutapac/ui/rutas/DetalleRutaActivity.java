package pe.edu.usat.ecorutapac.ui.rutas;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import pe.edu.usat.ecorutapac.R;
import pe.edu.usat.ecorutapac.data.local.entity.PuntoRutaEntity;
import pe.edu.usat.ecorutapac.data.local.entity.RutaEntity;
import pe.edu.usat.ecorutapac.databinding.ActivityDetalleRutaBinding;
import pe.edu.usat.ecorutapac.ui.atender.AtenderPuntoActivity;
import pe.edu.usat.ecorutapac.ui.common.UiState;
import pe.edu.usat.ecorutapac.ui.fiscalizador.ObservacionControlActivity;
import pe.edu.usat.ecorutapac.ui.incidencias.ListaIncidenciasActivity;
import pe.edu.usat.ecorutapac.ui.incidencias.ReportarIncidenciaActivity;
import pe.edu.usat.ecorutapac.ui.mapa.MapaRutaActivity;
import pe.edu.usat.ecorutapac.ui.rutas.adapter.PuntoRutaAdapter;
import pe.edu.usat.ecorutapac.ui.supervisor.AsignarRutaActivity;
import pe.edu.usat.ecorutapac.util.LocationHelper;
import pe.edu.usat.ecorutapac.util.SessionManager;

public class DetalleRutaActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 2001;

    private ActivityDetalleRutaBinding binding;
    private DetalleRutaViewModel viewModel;
    private PuntoRutaAdapter adapter;
    private LocationHelper locationHelper;
    private SessionManager sessionManager;
    private int idRutaActual = 1;
    private RutaEntity rutaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetalleRutaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        locationHelper = new LocationHelper(this);
        viewModel = new ViewModelProvider(this).get(DetalleRutaViewModel.class);

        procesarIntent(getIntent());

        setupToolbar();
        setupRecyclerView();
        setupObservers();
        setupListeners();
        configurarVistasPorRol();
        verificarPermisosUbicacion();
    }

    private void procesarIntent(Intent intent) {
        if (intent != null) {
            // Caso 1: Intent explícito con extra
            idRutaActual = intent.getIntExtra("id_ruta", 1);

            // Caso 2: Deep Link desde FCM (Reto R4)
            Uri data = intent.getData();
            if (data != null && "ecoruta".equalsIgnoreCase(data.getScheme())) {
                String idParam = data.getQueryParameter("id");
                if (idParam != null) {
                    try {
                        idRutaActual = Integer.parseInt(idParam);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new PuntoRutaAdapter(new PuntoRutaAdapter.OnPuntoActionListener() {
            @Override
            public void onAtenderClick(PuntoRutaEntity punto) {
                Intent intent = new Intent(DetalleRutaActivity.this, AtenderPuntoActivity.class);
                intent.putExtra("id_ruta", idRutaActual);
                intent.putExtra("punto", punto);
                startActivity(intent);
            }

            @Override
            public void onIncidenciaClick(PuntoRutaEntity punto) {
                Intent intent = new Intent(DetalleRutaActivity.this, ReportarIncidenciaActivity.class);
                intent.putExtra("id_ruta", idRutaActual);
                intent.putExtra("punto", punto);
                startActivity(intent);
            }
        });

        binding.rvPuntos.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPuntos.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getRutaState().observe(this, state -> {
            if (state != null && state.getEstado() == UiState.Estado.CONTENT) {
                rutaActual = state.getData();
                actualizarUiRuta(rutaActual);
            }
        });

        viewModel.getPuntosState().observe(this, state -> {
            if (state == null) return;
            if (state.getEstado() == UiState.Estado.LOADING) {
                binding.pbLoadingPuntos.setVisibility(View.VISIBLE);
            } else if (state.getEstado() == UiState.Estado.CONTENT) {
                binding.pbLoadingPuntos.setVisibility(View.GONE);
                adapter.setPuntos(state.getData());
            } else if (state.getEstado() == UiState.Estado.ERROR) {
                binding.pbLoadingPuntos.setVisibility(View.GONE);
                Toast.makeText(this, state.getMensaje(), Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getAccionMensaje().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });
    }

    private void actualizarUiRuta(RutaEntity ruta) {
        binding.tvCodigo.setText(ruta.getCodigo());
        binding.tvEstado.setText(ruta.getEstado());
        binding.tvFechaTurno.setText("Fecha: " + ruta.getFecha() + " | Turno: " + ruta.getTurno());
        binding.tvOperarioVehiculo.setText("Operario: " + (ruta.getNombreOperario() != null ? ruta.getNombreOperario() : "Sin asignar") +
                " | Vehículo: " + (ruta.getPlacaVehiculo() != null ? ruta.getPlacaVehiculo() : "Sin asignar"));
        binding.pbAvance.setProgress(ruta.getPorcentajeAvance());
        binding.tvPorcentaje.setText(ruta.getPorcentajeAvance() + "% completado (" + ruta.getPuntosAtendidos() + "/" + ruta.getTotalPuntos() + ")");

        // Estado de los botones según estado de la ruta
        if ("FINALIZADA".equalsIgnoreCase(ruta.getEstado())) {
            binding.btnIniciarRuta.setEnabled(false);
            binding.btnFinalizarRuta.setEnabled(false);
        } else if ("EN CURSO".equalsIgnoreCase(ruta.getEstado())) {
            binding.btnIniciarRuta.setEnabled(false);
            binding.btnFinalizarRuta.setEnabled(true);
        } else {
            binding.btnIniciarRuta.setEnabled(true);
            binding.btnFinalizarRuta.setEnabled(false);
        }
    }

    private void configurarVistasPorRol() {
        String rol = sessionManager.getRol();
        if ("SUPERVISOR".equalsIgnoreCase(rol)) {
            binding.btnAsignarRuta.setVisibility(View.VISIBLE);
        } else if ("FISCALIZADOR".equalsIgnoreCase(rol)) {
            binding.btnObservacionFiscalizador.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        binding.btnIniciarRuta.setOnClickListener(v -> viewModel.iniciarRuta(idRutaActual));
        binding.btnFinalizarRuta.setOnClickListener(v -> viewModel.finalizarRuta(idRutaActual));

        binding.btnVerMapa.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapaRutaActivity.class);
            intent.putExtra("id_ruta", idRutaActual);
            startActivity(intent);
        });

        binding.btnVerIncidencias.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListaIncidenciasActivity.class);
            intent.putExtra("id_ruta", idRutaActual);
            startActivity(intent);
        });

        binding.btnAsignarRuta.setOnClickListener(v -> {
            Intent intent = new Intent(this, AsignarRutaActivity.class);
            intent.putExtra("id_ruta", idRutaActual);
            startActivity(intent);
        });

        binding.btnObservacionFiscalizador.setOnClickListener(v -> {
            Intent intent = new Intent(this, ObservacionControlActivity.class);
            intent.putExtra("id_ruta", idRutaActual);
            startActivity(intent);
        });
    }

    private void verificarPermisosUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            obtenerUbicacion();
        }
    }

    private void obtenerUbicacion() {
        locationHelper.obtenerUbicacionActual(new LocationHelper.LocationResultCallback() {
            @Override
            public void onLocationObtained(Location location) {
                adapter.setUbicacionActual(location);
            }

            @Override
            public void onError(String message) {
                // Silencioso o log
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.cargarDetalleYPuntos(idRutaActual);
        obtenerUbicacion();
    }
}
