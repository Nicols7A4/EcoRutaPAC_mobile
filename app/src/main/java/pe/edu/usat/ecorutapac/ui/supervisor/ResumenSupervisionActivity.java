package pe.edu.usat.ecorutapac.ui.supervisor;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import pe.edu.usat.ecorutapac.data.remote.dto.response.ApiResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ResumenSupervisionResponse;
import pe.edu.usat.ecorutapac.data.repository.SupervisionRepository;
import pe.edu.usat.ecorutapac.databinding.ActivityResumenSupervisionBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResumenSupervisionActivity extends AppCompatActivity {

    private ActivityResumenSupervisionBinding binding;
    private SupervisionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResumenSupervisionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new SupervisionRepository(this);

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnActualizarKpis.setOnClickListener(v -> cargarKpis());

        cargarKpis();
    }

    private void cargarKpis() {
        repository.obtenerResumen(null, new Callback<ApiResponse<ResumenSupervisionResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ResumenSupervisionResponse>> call, Response<ApiResponse<ResumenSupervisionResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ResumenSupervisionResponse data = response.body().getData();
                    binding.tvKpiTotalRutas.setText(String.valueOf(data.getTotalRutas()));
                    binding.tvKpiEnCurso.setText(String.valueOf(data.getRutasEnCurso()));
                    binding.tvKpiPuntosAtendidos.setText(data.getPuntosAtendidos() + " / " + data.getTotalPuntos());
                    binding.tvKpiIncidencias.setText(String.valueOf(data.getTotalIncidencias()));
                } else {
                    Toast.makeText(ResumenSupervisionActivity.this, "No se pudieron obtener métricas actualizadas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ResumenSupervisionResponse>> call, Throwable t) {
                Toast.makeText(ResumenSupervisionActivity.this, "Error de red al cargar KPIs: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
