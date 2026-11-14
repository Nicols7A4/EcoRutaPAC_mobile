package pe.edu.usat.ecorutapac.ui.incidencias;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;

import pe.edu.usat.ecorutapac.data.local.entity.PuntoRutaEntity;
import pe.edu.usat.ecorutapac.databinding.ActivityReportarIncidenciaBinding;
import pe.edu.usat.ecorutapac.ui.common.UiState;
import pe.edu.usat.ecorutapac.util.ImageCompressor;

public class ReportarIncidenciaActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 5001;

    private ActivityReportarIncidenciaBinding binding;
    private ReportarIncidenciaViewModel viewModel;
    private PuntoRutaEntity punto;
    private int idRuta;

    private Uri fotoCamaraUri;
    private File fotoComprimida;

    private final String[] tiposIncidencia = {
            "Acceso bloqueado",
            "Contenedor dañado",
            "Residuos no disponibles",
            "Exceso de residuos",
            "Vehículo",
            "Otro"
    };

    private final ActivityResultLauncher<Uri> tomarFotoLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && fotoCamaraUri != null) {
                    procesarImagenSeleccionada(fotoCamaraUri);
                }
            }
    );

    private final ActivityResultLauncher<String> galeriaLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    procesarImagenSeleccionada(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportarIncidenciaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ReportarIncidenciaViewModel.class);

        idRuta = getIntent().getIntExtra("id_ruta", 1);
        punto = (PuntoRutaEntity) getIntent().getSerializableExtra("punto");

        setupToolbar();
        setupSpinner();
        setupListeners();
        setupObservers();

        if (punto != null) {
            binding.tvPuntoAsociado.setText("Punto asociado: " + punto.getNombre() + " (" + punto.getCodigoQr() + ")");
        } else {
            binding.tvPuntoAsociado.setText("Incidencia general de la ruta ID " + idRuta);
        }
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tiposIncidencia);
        binding.spTipoIncidencia.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnTomarFoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                abrirCamara();
            }
        });

        binding.btnSeleccionarGaleria.setOnClickListener(v -> galeriaLauncher.launch("image/*"));

        binding.btnEnviarIncidencia.setOnClickListener(v -> {
            String tipo = (String) binding.spTipoIncidencia.getSelectedItem();
            String desc = binding.etDescripcion.getText() != null ? binding.etDescripcion.getText().toString().trim() : "";
            int idPunto = punto != null ? punto.getId() : 1;
            String nombrePunto = punto != null ? punto.getNombre() : "Ruta General";

            viewModel.enviarIncidencia(idRuta, idPunto, nombrePunto, tipo, desc, fotoComprimida);
        });
    }

    private void abrirCamara() {
        try {
            File cachePath = new File(getCacheDir(), "images");
            if (!cachePath.exists()) cachePath.mkdirs();
            File archivoFotoTemp = File.createTempFile("CAPTURA_", ".jpg", cachePath);
            fotoCamaraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", archivoFotoTemp);
            tomarFotoLauncher.launch(fotoCamaraUri);
        } catch (IOException e) {
            Toast.makeText(this, "Error al preparar archivo de cámara: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Reto R3: Compresión en segundo plano
    private void procesarImagenSeleccionada(Uri uri) {
        binding.tvCompresionInfo.setVisibility(View.VISIBLE);
        binding.tvCompresionInfo.setText("Comprimiendo imagen en segundo plano...");

        ImageCompressor.comprimirAsync(this, uri, new ImageCompressor.CompressionCallback() {
            @Override
            public void onSuccess(File compressed) {
                runOnUiThread(() -> {
                    fotoComprimida = compressed;
                    binding.ivPreviewFoto.setVisibility(View.VISIBLE);
                    Glide.with(ReportarIncidenciaActivity.this).load(compressed).into(binding.ivPreviewFoto);
                    long kb = compressed.length() / 1024;
                    binding.tvCompresionInfo.setText("✓ Evidencia comprimida con éxito: " + kb + " KB (óptimo para subida multipart)");
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    binding.tvCompresionInfo.setText("Error al comprimir: " + e.getMessage());
                });
            }
        });
    }

    private void setupObservers() {
        viewModel.getReporteState().observe(this, state -> {
            if (state == null) return;

            if (state.getEstado() == UiState.Estado.LOADING) {
                binding.pbCargando.setVisibility(View.VISIBLE);
                binding.btnEnviarIncidencia.setEnabled(false);
            } else {
                binding.pbCargando.setVisibility(View.GONE);
                binding.btnEnviarIncidencia.setEnabled(true);
            }

            if (state.getEstado() == UiState.Estado.CONTENT) {
                if (state.isDesdeCache()) {
                    Toast.makeText(this, "Incidencia y foto guardadas OFFLINE. Se subirán al recuperar red (Reto R1/R3).", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Incidencia enviada al backend por multipart exitosamente", Toast.LENGTH_LONG).show();
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
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        }
    }
}
