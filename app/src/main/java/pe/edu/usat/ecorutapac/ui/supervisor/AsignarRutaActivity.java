package pe.edu.usat.ecorutapac.ui.supervisor;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import pe.edu.usat.ecorutapac.data.local.entity.RutaEntity;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ApiResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.OperarioItem;
import pe.edu.usat.ecorutapac.data.remote.dto.response.OperariosResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.VehiculoItem;
import pe.edu.usat.ecorutapac.data.remote.dto.response.VehiculosResponse;
import pe.edu.usat.ecorutapac.data.repository.RutaRepository;
import pe.edu.usat.ecorutapac.data.repository.SupervisionRepository;
import pe.edu.usat.ecorutapac.databinding.ActivityAsignarRutaBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AsignarRutaActivity extends AppCompatActivity {

    private ActivityAsignarRutaBinding binding;
    private SupervisionRepository supervisionRepo;
    private RutaRepository rutaRepo;
    private int idRuta;

    private List<OperarioItem> listaOperarios = new ArrayList<>();
    private List<VehiculoItem> listaVehiculos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAsignarRutaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        idRuta = getIntent().getIntExtra("id_ruta", 1);
        supervisionRepo = new SupervisionRepository(this);
        rutaRepo = new RutaRepository(this);

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tvRutaInfo.setText("Asignación de Ruta ID: " + idRuta);

        cargarSelectores();
        setupListeners();
    }

    private void cargarSelectores() {
        binding.pbCargando.setVisibility(View.VISIBLE);

        supervisionRepo.obtenerOperarios(new Callback<ApiResponse<OperariosResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<OperariosResponse>> call, Response<ApiResponse<OperariosResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listaOperarios = response.body().getData().getOperarios();
                    ArrayAdapter<OperarioItem> adapterOp = new ArrayAdapter<>(AsignarRutaActivity.this, android.R.layout.simple_spinner_dropdown_item, listaOperarios);
                    binding.spOperarios.setAdapter(adapterOp);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OperariosResponse>> call, Throwable t) {}
        });

        supervisionRepo.obtenerVehiculos(new Callback<ApiResponse<VehiculosResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<VehiculosResponse>> call, Response<ApiResponse<VehiculosResponse>> response) {
                binding.pbCargando.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    listaVehiculos = response.body().getData().getVehiculos();
                    ArrayAdapter<VehiculoItem> adapterVeh = new ArrayAdapter<>(AsignarRutaActivity.this, android.R.layout.simple_spinner_dropdown_item, listaVehiculos);
                    binding.spVehiculos.setAdapter(adapterVeh);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<VehiculosResponse>> call, Throwable t) {
                binding.pbCargando.setVisibility(View.GONE);
            }
        });
    }

    private void setupListeners() {
        binding.btnConfirmarAsignacion.setOnClickListener(v -> {
            if (listaOperarios.isEmpty() || listaVehiculos.isEmpty()) {
                Toast.makeText(this, "Debe seleccionar un operario y un vehículo válidos", Toast.LENGTH_SHORT).show();
                return;
            }

            OperarioItem op = (OperarioItem) binding.spOperarios.getSelectedItem();
            VehiculoItem veh = (VehiculoItem) binding.spVehiculos.getSelectedItem();

            if (op == null || veh == null) return;

            binding.pbCargando.setVisibility(View.VISIBLE);
            rutaRepo.asignarRuta(idRuta, op.getId(), veh.getId(), new Callback<ApiResponse<RutaEntity>>() {
                @Override
                public void onResponse(Call<ApiResponse<RutaEntity>> call, Response<ApiResponse<RutaEntity>> response) {
                    binding.pbCargando.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(AsignarRutaActivity.this, "Ruta asignada exitosamente a " + op.getNombre(), Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(AsignarRutaActivity.this, "Error al asignar: HTTP " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<RutaEntity>> call, Throwable t) {
                    binding.pbCargando.setVisibility(View.GONE);
                    Toast.makeText(AsignarRutaActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
