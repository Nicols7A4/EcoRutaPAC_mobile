package pe.edu.usat.ecorutapac.ui.rutas;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.List;

import pe.edu.usat.ecorutapac.data.local.entity.PuntoRutaEntity;
import pe.edu.usat.ecorutapac.data.local.entity.RutaEntity;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ApiResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.PuntosResponse;
import pe.edu.usat.ecorutapac.data.repository.RutaRepository;
import pe.edu.usat.ecorutapac.ui.common.UiState;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleRutaViewModel extends AndroidViewModel {

    private final RutaRepository rutaRepository;
    private final MutableLiveData<UiState<RutaEntity>> rutaState = new MutableLiveData<>();
    private final MutableLiveData<UiState<List<PuntoRutaEntity>>> puntosState = new MutableLiveData<>();
    private final MutableLiveData<String> accionMensaje = new MutableLiveData<>();

    public DetalleRutaViewModel(@NonNull Application application) {
        super(application);
        this.rutaRepository = new RutaRepository(application);
    }

    public LiveData<UiState<RutaEntity>> getRutaState() {
        return rutaState;
    }

    public LiveData<UiState<List<PuntoRutaEntity>>> getPuntosState() {
        return puntosState;
    }

    public LiveData<String> getAccionMensaje() {
        return accionMensaje;
    }

    public void cargarDetalleYPuntos(int idRuta) {
        rutaState.setValue(UiState.loading());
        puntosState.setValue(UiState.loading());

        // 1. Cargar detalle general de ruta
        rutaRepository.obtenerDetalleRuta(idRuta, new Callback<ApiResponse<RutaEntity>>() {
            @Override
            public void onResponse(Call<ApiResponse<RutaEntity>> call, Response<ApiResponse<RutaEntity>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    RutaEntity ruta = response.body().getData();
                    rutaRepository.guardarRutasEnCache(Collections.singletonList(ruta));
                    rutaState.setValue(UiState.content(ruta, false));
                } else {
                    cargarRutaLocal(idRuta);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<RutaEntity>> call, Throwable t) {
                cargarRutaLocal(idRuta);
            }
        });

        // 2. Cargar puntos de la ruta
        rutaRepository.obtenerPuntosRutaRemotos(idRuta, new Callback<ApiResponse<PuntosResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PuntosResponse>> call, Response<ApiResponse<PuntosResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<PuntoRutaEntity> puntos = response.body().getData().getPuntos();
                    rutaRepository.guardarPuntosEnCache(puntos);
                    puntosState.setValue(UiState.content(puntos, false));
                } else {
                    cargarPuntosLocales(idRuta);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PuntosResponse>> call, Throwable t) {
                cargarPuntosLocales(idRuta);
            }
        });
    }

    private void cargarRutaLocal(int idRuta) {
        rutaRepository.obtenerRutasLocales(null, null, 0, rutas -> {
            RutaEntity encontrada = null;
            if (rutas != null) {
                for (RutaEntity r : rutas) {
                    if (r.getId() == idRuta) {
                        encontrada = r;
                        break;
                    }
                }
            }
            if (encontrada != null) {
                rutaState.postValue(UiState.content(encontrada, true));
            } else {
                rutaState.postValue(UiState.error("No se pudo cargar la información de la ruta"));
            }
        });
    }

    private void cargarPuntosLocales(int idRuta) {
        rutaRepository.obtenerPuntosLocales(idRuta, puntos -> {
            if (puntos != null && !puntos.isEmpty()) {
                puntosState.postValue(UiState.content(puntos, true));
            } else {
                puntosState.postValue(UiState.error("Sin puntos disponibles en caché local"));
            }
        });
    }

    public void iniciarRuta(int idRuta) {
        rutaRepository.iniciarRuta(idRuta, new Callback<ApiResponse<RutaEntity>>() {
            @Override
            public void onResponse(Call<ApiResponse<RutaEntity>> call, Response<ApiResponse<RutaEntity>> response) {
                accionMensaje.setValue("Ruta iniciada correctamente");
                cargarDetalleYPuntos(idRuta);
            }

            @Override
            public void onFailure(Call<ApiResponse<RutaEntity>> call, Throwable t) {
                accionMensaje.setValue("Ruta iniciada en modo local: " + t.getMessage());
                cargarDetalleYPuntos(idRuta);
            }
        });
    }

    public void finalizarRuta(int idRuta) {
        rutaRepository.finalizarRuta(idRuta, new Callback<ApiResponse<RutaEntity>>() {
            @Override
            public void onResponse(Call<ApiResponse<RutaEntity>> call, Response<ApiResponse<RutaEntity>> response) {
                if (response.isSuccessful()) {
                    accionMensaje.setValue("Ruta finalizada con éxito");
                    cargarDetalleYPuntos(idRuta);
                } else {
                    accionMensaje.setValue("No se puede finalizar: aún hay puntos pendientes sin atención ni justificación");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<RutaEntity>> call, Throwable t) {
                accionMensaje.setValue("Error de conexión al finalizar: " + t.getMessage());
            }
        });
    }
}
