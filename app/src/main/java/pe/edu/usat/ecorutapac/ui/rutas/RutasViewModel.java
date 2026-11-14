package pe.edu.usat.ecorutapac.ui.rutas;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import pe.edu.usat.ecorutapac.data.local.entity.RutaEntity;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ApiResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.RutasResponse;
import pe.edu.usat.ecorutapac.data.repository.RutaRepository;
import pe.edu.usat.ecorutapac.sync.SyncManager;
import pe.edu.usat.ecorutapac.ui.common.UiState;
import pe.edu.usat.ecorutapac.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RutasViewModel extends AndroidViewModel {

    private final RutaRepository rutaRepository;
    private final SessionManager sessionManager;
    private final MutableLiveData<UiState<List<RutaEntity>>> rutasState = new MutableLiveData<>();

    public RutasViewModel(@NonNull Application application) {
        super(application);
        this.rutaRepository = new RutaRepository(application);
        this.sessionManager = new SessionManager(application);
    }

    public LiveData<UiState<List<RutaEntity>>> getRutasState() {
        return rutasState;
    }

    public LiveData<Integer> getOperacionesPendientesCount() {
        return rutaRepository.observarOperacionesPendientes();
    }

    public void cargarRutas(String fecha, String estadoFiltro) {
        rutasState.setValue(UiState.loading());

        Integer idOperarioFiltro = null;
        if ("OPERARIO".equalsIgnoreCase(sessionManager.getRol())) {
            idOperarioFiltro = sessionManager.getUsuarioId();
        }

        String estadoParam = "TODAS".equalsIgnoreCase(estadoFiltro) ? null : estadoFiltro;

        // Intentar primero consumo remoto con Retrofit
        rutaRepository.obtenerRutasRemotas(fecha, estadoParam, idOperarioFiltro, new Callback<ApiResponse<RutasResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<RutasResponse>> call, @NonNull Response<ApiResponse<RutasResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<RutaEntity> lista = response.body().getData().getRutas();
                    if (lista == null || lista.isEmpty()) {
                        rutasState.setValue(UiState.empty());
                    } else {
                        // Guardar en caché Room
                        rutaRepository.guardarRutasEnCache(lista);
                        rutasState.setValue(UiState.content(lista, false));
                    }
                } else {
                    cargarDesdeCache(fecha, estadoParam);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<RutasResponse>> call, @NonNull Throwable t) {
                // Fallo de red: cargar desde caché local Room (Modo Offline)
                cargarDesdeCache(fecha, estadoParam);
            }
        });
    }

    private void cargarDesdeCache(String fecha, String estado) {
        int idOp = "OPERARIO".equalsIgnoreCase(sessionManager.getRol()) ? sessionManager.getUsuarioId() : 0;
        rutaRepository.obtenerRutasLocales(fecha, estado, idOp, rutasLocales -> {
            if (rutasLocales == null || rutasLocales.isEmpty()) {
                rutasState.postValue(UiState.error("Sin conexión a la red y sin rutas guardadas localmente"));
            } else {
                rutasState.postValue(UiState.content(rutasLocales, true));
            }
        });
    }

    public void forzarSincronizacion() {
        SyncManager.encolarSincronizacion(getApplication());
    }
}
