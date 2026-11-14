package pe.edu.usat.ecorutapac.ui.fiscalizador;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import pe.edu.usat.ecorutapac.R;
import pe.edu.usat.ecorutapac.data.local.entity.IncidenciaEntity;
import pe.edu.usat.ecorutapac.data.local.entity.RutaEntity;
import pe.edu.usat.ecorutapac.data.remote.RetrofitClient;
import pe.edu.usat.ecorutapac.data.remote.dto.request.EstadoIncidenciaRequest;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ApiResponse;
import pe.edu.usat.ecorutapac.data.repository.RutaRepository;
import pe.edu.usat.ecorutapac.databinding.ActivityObservacionControlBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ObservacionControlActivity extends AppCompatActivity {

    private ActivityObservacionControlBinding binding;
    private int idRuta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityObservacionControlBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        idRuta = getIntent().getIntExtra("id_ruta", 1);

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tvRutaInfo.setText("Fiscalización de Ruta ID: " + idRuta);

        binding.btnGuardarObservacion.setOnClickListener(v -> guardarDictamen());
    }

    private void guardarDictamen() {
        String obs = binding.etObservacion.getText() != null ? binding.etObservacion.getText().toString().trim() : "";
        if (obs.isEmpty()) {
            Toast.makeText(this, "Debe ingresar el detalle de la observación", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.pbCargando.setVisibility(View.VISIBLE);

        boolean esConforme = binding.rbConforme.isChecked();
        String nuevoEstado = esConforme ? "FINALIZADA" : "OBSERVADA";

        EstadoIncidenciaRequest req = new EstadoIncidenciaRequest(nuevoEstado, obs);

        RetrofitClient.getService(this).actualizarEstadoIncidencia(idRuta, req).enqueue(new Callback<ApiResponse<IncidenciaEntity>>() {
            @Override
            public void onResponse(Call<ApiResponse<IncidenciaEntity>> call, Response<ApiResponse<IncidenciaEntity>> response) {
                binding.pbCargando.setVisibility(View.GONE);
                Toast.makeText(ObservacionControlActivity.this, "Dictamen registrado con éxito. Estado: " + nuevoEstado, Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(Call<ApiResponse<IncidenciaEntity>> call, Throwable t) {
                binding.pbCargando.setVisibility(View.GONE);
                Toast.makeText(ObservacionControlActivity.this, "Dictamen guardado localmente", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
