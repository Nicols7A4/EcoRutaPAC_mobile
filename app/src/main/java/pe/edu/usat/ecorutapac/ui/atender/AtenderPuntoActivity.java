package pe.edu.usat.ecorutapac.ui.atender;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.Locale;

import pe.edu.usat.ecorutapac.R;
import pe.edu.usat.ecorutapac.data.local.entity.PuntoRutaEntity;
import pe.edu.usat.ecorutapac.databinding.ActivityAtenderPuntoBinding;
import pe.edu.usat.ecorutapac.ui.common.UiState;
import pe.edu.usat.ecorutapac.util.LocationHelper;

public class AtenderPuntoActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 3001;

    private ActivityAtenderPuntoBinding binding;
    private AtenderPuntoViewModel viewModel;
    private LocationHelper locationHelper;
    private PuntoRutaEntity punto;
    private int idRuta;

    private Location ubicacionActual;
    private boolean qrValido = false;
    private boolean dentroDelRadio = false;

    private final ActivityResultLauncher<Intent> scannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String codigoQrEscaneado = result.getData().getStringExtra("codigo_qr");
                    binding.etCodigoQr.setText(codigoQrEscaneado);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAtenderPuntoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AtenderPuntoViewModel.class);
        locationHelper = new LocationHelper(this);

        idRuta = getIntent().getIntExtra("id_ruta", 1);
        punto = (PuntoRutaEntity) getIntent().getSerializableExtra("punto");

        if (punto == null) {
            Toast.makeText(this, "Datos del punto no disponibles", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        poblarDatosPunto();
        setupQrWatcher();
        setupButtons();
        setupObservers();
        solicitarUbicacion();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void poblarDatosPunto() {
        binding.tvNombrePunto.setText(punto.getNombre());
        binding.tvDireccion.setText("Dirección: " + punto.getDireccion());
        binding.tvQrEsperado.setText("QR Requerido: " + punto.getCodigoQr());
    }

    private void setupQrWatcher() {
        binding.etCodigoQr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validarQr(s != null ? s.toString().trim() : "");
            }
        });
    }

    private void validarQr(String textoQr) {
        if (!TextUtils.isEmpty(textoQr) && textoQr.equalsIgnoreCase(punto.getCodigoQr())) {
            qrValido = true;
            binding.tvEstadoQr.setText("✓ Código QR válido y coincidente");
            binding.tvEstadoQr.setTextColor(ContextCompat.getColor(this, R.color.status_atendido));
        } else {
            qrValido = false;
            binding.tvEstadoQr.setText("✕ El código no coincide con el punto esperado (" + punto.getCodigoQr() + ")");
            binding.tvEstadoQr.setTextColor(ContextCompat.getColor(this, R.color.status_observada));
        }
        actualizarBotonConfirmar();
    }

    private void setupButtons() {
        binding.btnEscanearQr.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 4001);
            } else {
                abrirEscaner();
            }
        });

        binding.btnAutoSimularQr.setOnClickListener(v -> binding.etCodigoQr.setText(punto.getCodigoQr()));

        binding.btnActualizarGps.setOnClickListener(v -> solicitarUbicacion());

        // Botón para simular ubicación exacta del punto (muy útil para sustentar en laboratorio USAT)
        binding.btnSimularUbicacionEnSitio.setOnClickListener(v -> {
            Location locSimulada = new Location("simulada");
            locSimulada.setLatitude(punto.getLatitud());
            locSimulada.setLongitude(punto.getLongitud());
            procesarUbicacion(locSimulada);
            Toast.makeText(this, "Simulando ubicación en el punto exacto", Toast.LENGTH_SHORT).show();
        });

        binding.btnConfirmarAtencion.setOnClickListener(v -> {
            if (!qrValido) {
                Toast.makeText(this, "Debe escanear y validar el QR del contenedor primero", Toast.LENGTH_LONG).show();
                return;
            }
            if (!dentroDelRadio) {
                Toast.makeText(this, "Bloqueado: Debe encontrarse a menos de 50m del punto", Toast.LENGTH_LONG).show();
                return;
            }

            double lat = ubicacionActual != null ? ubicacionActual.getLatitude() : punto.getLatitud();
            double lon = ubicacionActual != null ? ubicacionActual.getLongitude() : punto.getLongitud();
            String obs = binding.etObservacion.getText() != null ? binding.etObservacion.getText().toString().trim() : "";

            viewModel.confirmarAtencion(idRuta, punto.getId(), punto.getCodigoQr(), lat, lon, obs);
        });
    }

    private void abrirEscaner() {
        Intent intent = new Intent(this, ScannerQRActivity.class);
        scannerLauncher.launch(intent);
    }

    private void solicitarUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }

        binding.tvCoordenadasActuales.setText("Ubicación actual: Leyendo GPS de alta precisión...");
        locationHelper.obtenerUbicacionActual(new LocationHelper.LocationResultCallback() {
            @Override
            public void onLocationObtained(Location location) {
                procesarUbicacion(location);
            }

            @Override
            public void onError(String message) {
                binding.tvCoordenadasActuales.setText("GPS no disponible: " + message);
            }
        });
    }

    // Reto R2: Validación de Proximidad
    private void procesarUbicacion(Location location) {
        this.ubicacionActual = location;
        binding.tvCoordenadasActuales.setText(String.format(Locale.getDefault(), "Ubicación: Lat %.6f, Lon %.6f", location.getLatitude(), location.getLongitude()));

        int radioMax = punto.getRadioValidacion() > 0 ? punto.getRadioValidacion() : 50;
        float distMetros = LocationHelper.calcularDistanciaMetros(location.getLatitude(), location.getLongitude(), punto.getLatitud(), punto.getLongitud());

        binding.tvDistanciaCalculada.setText(String.format(Locale.getDefault(), "Distancia al punto: %.1f metros (Radio permitido: %d m)", distMetros, radioMax));

        dentroDelRadio = distMetros <= radioMax;
        if (dentroDelRadio) {
            binding.tvEstadoProximidad.setText("✓ Dentro del radio permitido para confirmación");
            binding.tvEstadoProximidad.setTextColor(ContextCompat.getColor(this, R.color.status_atendido));
        } else {
            binding.tvEstadoProximidad.setText(String.format(Locale.getDefault(), "✕ Fuera de radio: a %.1f m (Máx: %d m). Confirmación bloqueada.", distMetros, radioMax));
            binding.tvEstadoProximidad.setTextColor(ContextCompat.getColor(this, R.color.status_observada));
        }
        actualizarBotonConfirmar();
    }

    private void actualizarBotonConfirmar() {
        binding.btnConfirmarAtencion.setEnabled(qrValido && dentroDelRadio);
    }

    private void setupObservers() {
        viewModel.getAtencionState().observe(this, state -> {
            if (state == null) return;
            if (state.getEstado() == UiState.Estado.LOADING) {
                binding.pbConfirmar.setVisibility(View.VISIBLE);
                binding.btnConfirmarAtencion.setEnabled(false);
            } else {
                binding.pbConfirmar.setVisibility(View.GONE);
                binding.btnConfirmarAtencion.setEnabled(true);
            }

            if (state.getEstado() == UiState.Estado.CONTENT) {
                if (state.isDesdeCache()) {
                    Toast.makeText(this, "Atención registrada OFFLINE. Se sincronizará automáticamente al detectar red (Reto R1).", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Atención registrada y sincronizada en el servidor con éxito", Toast.LENGTH_LONG).show();
                }
                finish();
            } else if (state.getEstado() == UiState.Estado.ERROR) {
                Toast.makeText(this, state.getMensaje(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            solicitarUbicacion();
        } else if (requestCode == 4001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirEscaner();
        }
    }
}
