package pe.edu.usat.ecorutapac.ui.incidencias;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import pe.edu.usat.ecorutapac.data.local.entity.IncidenciaEntity;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ApiResponse;
import pe.edu.usat.ecorutapac.data.repository.IncidenciaRepository;
import pe.edu.usat.ecorutapac.databinding.ActivityListaIncidenciasBinding;
import pe.edu.usat.ecorutapac.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaIncidenciasActivity extends AppCompatActivity {

    private ActivityListaIncidenciasBinding binding;
    private IncidenciaRepository repository;
    private IncidenciaAdapter adapter;
    private int idRuta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListaIncidenciasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        idRuta = getIntent().getIntExtra("id_ruta", 1);
        repository = new IncidenciaRepository(this);
        SessionManager sm = new SessionManager(this);

        boolean esSupervisorOFiscal = "SUPERVISOR".equalsIgnoreCase(sm.getRol()) || "FISCALIZADOR".equalsIgnoreCase(sm.getRol());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new IncidenciaAdapter(esSupervisorOFiscal, incidencia -> {
            // Acción para cambiar estado a RESUELTA
            repository.actualizarEstado(incidencia.getId(), "RESUELTA", "Revisada por supervisión", new Callback<ApiResponse<IncidenciaEntity>>() {
                @Override
                public void onResponse(Call<ApiResponse<IncidenciaEntity>> call, Response<ApiResponse<IncidenciaEntity>> response) {
                    Toast.makeText(ListaIncidenciasActivity.this, "Incidencia marcada como RESUELTA", Toast.LENGTH_SHORT).show();
                    cargarIncidencias();
                }

                @Override
                public void onFailure(Call<ApiResponse<IncidenciaEntity>> call, Throwable t) {
                    Toast.makeText(ListaIncidenciasActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.rvIncidencias.setLayoutManager(new LinearLayoutManager(this));
        binding.rvIncidencias.setAdapter(adapter);

        cargarIncidencias();
    }

    private void cargarIncidencias() {
        binding.progressBar.setVisibility(View.VISIBLE);
        repository.obtenerIncidencias(idRuta, incidencias -> runOnUiThread(() -> {
            binding.progressBar.setVisibility(View.GONE);
            if (incidencias == null || incidencias.isEmpty()) {
                binding.tvEmptyIncidencias.setVisibility(View.VISIBLE);
                adapter.setIncidencias(null);
            } else {
                binding.tvEmptyIncidencias.setVisibility(View.GONE);
                adapter.setIncidencias(incidencias);
            }
        }));
    }
}
