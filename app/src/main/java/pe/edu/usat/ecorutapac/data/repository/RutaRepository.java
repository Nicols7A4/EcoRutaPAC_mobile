package pe.edu.usat.ecorutapac.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pe.edu.usat.ecorutapac.data.local.dao.OperacionPendienteDao;
import pe.edu.usat.ecorutapac.data.local.dao.PuntoRutaDao;
import pe.edu.usat.ecorutapac.data.local.dao.RutaDao;
import pe.edu.usat.ecorutapac.data.local.database.EcoRutaDatabase;
import pe.edu.usat.ecorutapac.data.local.entity.OperacionPendienteEntity;
import pe.edu.usat.ecorutapac.data.local.entity.PuntoRutaEntity;
import pe.edu.usat.ecorutapac.data.local.entity.RutaEntity;
import pe.edu.usat.ecorutapac.data.remote.RetrofitClient;
import pe.edu.usat.ecorutapac.data.remote.api.ApiService;
import pe.edu.usat.ecorutapac.data.remote.dto.request.AsignarRutaRequest;
import pe.edu.usat.ecorutapac.data.remote.dto.request.AtenderPuntoRequest;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ApiResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.PuntosResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.RutasResponse;
import pe.edu.usat.ecorutapac.sync.SyncManager;
import pe.edu.usat.ecorutapac.util.NetworkUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RutaRepository {

    private static final String TAG = "RutaRepository";

    public interface LocalCallback<T> {
        void onResult(T data);
    }

    public interface AtencionCallback {
        void onSuccess(PuntoRutaEntity punto, boolean fueOffline);
        void onError(String mensaje);
    }

    private final Context context;
    private final RutaDao rutaDao;
    private final PuntoRutaDao puntoRutaDao;
    private final OperacionPendienteDao operacionDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    public RutaRepository(Context context) {
        this.context = context.getApplicationContext();
        EcoRutaDatabase db = EcoRutaDatabase.getInstance(this.context);
        this.rutaDao = db.rutaDao();
        this.puntoRutaDao = db.puntoRutaDao();
        this.operacionDao = db.operacionPendienteDao();
    }

    private ApiService getApiService() {
        return RetrofitClient.getService(context);
    }

    private static <T> Callback<T> wrapCallback(Callback<T> callback) {
        if (callback != null) {
            return callback;
        }
        return new Callback<>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {}
            @Override
            public void onFailure(Call<T> call, Throwable t) {}
        };
    }

    // --- RUTAS: REMOTO & LOCAL ---

    public void obtenerRutasRemotas(String fecha, String estado, Integer idOperario, Callback<ApiResponse<RutasResponse>> callback) {
        getApiService().obtenerRutas(fecha, estado, idOperario).enqueue(wrapCallback(callback));
    }

    public void obtenerRutasLocales(String fecha, String estado, int idOperario, LocalCallback<List<RutaEntity>> callback) {
        executor.execute(() -> {
            List<RutaEntity> lista = rutaDao.filtrarRutas(fecha, estado, idOperario);
            callback.onResult(lista);
        });
    }

    public void guardarRutasEnCache(List<RutaEntity> rutas) {
        executor.execute(() -> rutaDao.insertarTodas(rutas));
    }

    public void obtenerDetalleRuta(int idRuta, Callback<ApiResponse<RutaEntity>> callback) {
        getApiService().obtenerDetalleRuta(idRuta).enqueue(wrapCallback(callback));
    }

    public void obtenerPuntosRutaRemotos(int idRuta, Callback<ApiResponse<PuntosResponse>> callback) {
        getApiService().obtenerPuntosRuta(idRuta).enqueue(wrapCallback(callback));
    }

    public void obtenerPuntosLocales(int idRuta, LocalCallback<List<PuntoRutaEntity>> callback) {
        executor.execute(() -> {
            List<PuntoRutaEntity> puntos = puntoRutaDao.obtenerPuntosPorRuta(idRuta);
            callback.onResult(puntos);
        });
    }

    public void guardarPuntosEnCache(List<PuntoRutaEntity> puntos) {
        executor.execute(() -> puntoRutaDao.insertarTodos(puntos));
    }

    public void iniciarRuta(int idRuta, Callback<ApiResponse<RutaEntity>> callback) {
        getApiService().iniciarRuta(idRuta).enqueue(new Callback<ApiResponse<RutaEntity>>() {
            @Override
            public void onResponse(Call<ApiResponse<RutaEntity>> call, Response<ApiResponse<RutaEntity>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    executor.execute(() -> rutaDao.actualizarEstado(idRuta, "EN CURSO"));
                }
                if (callback != null) {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<RutaEntity>> call, Throwable t) {
                // Operación local en caso de desconexión
                executor.execute(() -> rutaDao.actualizarEstado(idRuta, "EN CURSO"));
                if (callback != null) {
                    callback.onFailure(call, t);
                }
            }
        });
    }

    public void finalizarRuta(int idRuta, Callback<ApiResponse<RutaEntity>> callback) {
        getApiService().finalizarRuta(idRuta).enqueue(new Callback<ApiResponse<RutaEntity>>() {
            @Override
            public void onResponse(Call<ApiResponse<RutaEntity>> call, Response<ApiResponse<RutaEntity>> response) {
                if (response.isSuccessful()) {
                    executor.execute(() -> rutaDao.actualizarEstado(idRuta, "FINALIZADA"));
                }
                if (callback != null) {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<RutaEntity>> call, Throwable t) {
                if (callback != null) {
                    callback.onFailure(call, t);
                }
            }
        });
    }

    public void asignarRuta(int idRuta, int idOperario, int idVehiculo, Callback<ApiResponse<RutaEntity>> callback) {
        AsignarRutaRequest req = new AsignarRutaRequest(idOperario, idVehiculo);
        getApiService().asignarRuta(idRuta, req).enqueue(wrapCallback(callback));
    }

    // --- ATENCIÓN DE PUNTO TRANSACCIONAL (MÓDULO 3 & RETO R1) ---

    public void atenderPunto(int idRuta, int idRutaPunto, String codigoQr, double lat, double lon, String observacion, AtencionCallback callback) {
        String uuidOperacion = UUID.randomUUID().toString();
        String fechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        AtenderPuntoRequest req = new AtenderPuntoRequest(codigoQr, lat, lon, observacion, uuidOperacion);

        if (!NetworkUtils.hayConexion(context)) {
            // Guardar localmente como PENDIENTE
            guardarAtencionPendienteOffline(idRuta, idRutaPunto, req, fechaHora, callback);
            return;
        }

        // Intento online
        getApiService().atenderPunto(idRutaPunto, req).enqueue(new Callback<ApiResponse<PuntoRutaEntity>>() {
            @Override
            public void onResponse(Call<ApiResponse<PuntoRutaEntity>> call, Response<ApiResponse<PuntoRutaEntity>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    PuntoRutaEntity puntoActualizado = response.body().getData();
                    executor.execute(() -> {
                        puntoRutaDao.marcarAtendido(idRutaPunto, "ATENDIDO", fechaHora, lat, lon, observacion);
                        int atendidos = puntoRutaDao.contarAtendidosPorRuta(idRuta);
                        int total = puntoRutaDao.contarTotalPorRuta(idRuta);
                        int porcentaje = total > 0 ? (atendidos * 100 / total) : 0;
                        rutaDao.actualizarAvance(idRuta, atendidos, porcentaje);
                    });
                    if (callback != null) callback.onSuccess(puntoActualizado, false);
                } else if (response.code() == 409) {
                    // Idempotencia: el servidor ya lo había atendido
                    executor.execute(() -> puntoRutaDao.marcarAtendido(idRutaPunto, "ATENDIDO", fechaHora, lat, lon, observacion));
                    if (callback != null) callback.onSuccess(null, false);
                } else {
                    if (callback != null) callback.onError("Error en servidor: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PuntoRutaEntity>> call, Throwable t) {
                // Falla de red: guardar en pendientes y encolar Worker
                Log.w(TAG, "Fallo remoto al atender punto, derivando a modo offline: " + t.getMessage());
                guardarAtencionPendienteOffline(idRuta, idRutaPunto, req, fechaHora, callback);
            }
        });
    }

    private void guardarAtencionPendienteOffline(int idRuta, int idRutaPunto, AtenderPuntoRequest req, String fechaHora, AtencionCallback callback) {
        executor.execute(() -> {
            OperacionPendienteEntity op = new OperacionPendienteEntity();
            op.setUuid(req.getUuidOperacion());
            op.setTipoOperacion("ATENDER_PUNTO");
            op.setIdRuta(idRuta);
            op.setIdRutaPunto(idRutaPunto);
            op.setPayloadJson(gson.toJson(req));
            op.setEstado("PENDIENTE");
            op.setFechaCreacion(fechaHora);
            op.setReintentos(0);

            operacionDao.insertar(op);
            puntoRutaDao.marcarAtendido(idRutaPunto, "ATENDIDO", fechaHora, req.getLatitud(), req.getLongitud(), req.getObservacion());

            int atendidos = puntoRutaDao.contarAtendidosPorRuta(idRuta);
            int total = puntoRutaDao.contarTotalPorRuta(idRuta);
            int porcentaje = total > 0 ? (atendidos * 100 / total) : 0;
            rutaDao.actualizarAvance(idRuta, atendidos, porcentaje);

            // Programar WorkManager para cuando regrese la red
            SyncManager.encolarSincronizacion(context);

            if (callback != null) {
                callback.onSuccess(puntoRutaDao.obtenerPorId(idRutaPunto), true);
            }
        });
    }

    public LiveData<Integer> observarOperacionesPendientes() {
        return operacionDao.observarTotalPendientes();
    }
}
