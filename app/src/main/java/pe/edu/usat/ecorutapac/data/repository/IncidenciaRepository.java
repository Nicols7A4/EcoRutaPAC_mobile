package pe.edu.usat.ecorutapac.data.repository;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pe.edu.usat.ecorutapac.data.local.dao.IncidenciaDao;
import pe.edu.usat.ecorutapac.data.local.dao.OperacionPendienteDao;
import pe.edu.usat.ecorutapac.data.local.dao.PuntoRutaDao;
import pe.edu.usat.ecorutapac.data.local.database.EcoRutaDatabase;
import pe.edu.usat.ecorutapac.data.local.entity.IncidenciaEntity;
import pe.edu.usat.ecorutapac.data.local.entity.OperacionPendienteEntity;
import pe.edu.usat.ecorutapac.data.remote.RetrofitClient;
import pe.edu.usat.ecorutapac.data.remote.api.ApiService;
import pe.edu.usat.ecorutapac.data.remote.dto.request.EstadoIncidenciaRequest;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ApiResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.IncidenciasResponse;
import pe.edu.usat.ecorutapac.sync.SyncManager;
import pe.edu.usat.ecorutapac.util.NetworkUtils;
import pe.edu.usat.ecorutapac.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncidenciaRepository {

    private static final String TAG = "IncidenciaRepository";

    public interface ReportarCallback {
        void onSuccess(IncidenciaEntity incidencia, boolean fueOffline);
        void onError(String mensaje);
    }

    public interface IncidenciasCallback {
        void onResult(List<IncidenciaEntity> incidencias);
    }

    private final Context context;
    private final IncidenciaDao incidenciaDao;
    private final PuntoRutaDao puntoRutaDao;
    private final OperacionPendienteDao operacionDao;
    private final SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    public IncidenciaRepository(Context context) {
        this.context = context.getApplicationContext();
        EcoRutaDatabase db = EcoRutaDatabase.getInstance(this.context);
        this.incidenciaDao = db.incidenciaDao();
        this.puntoRutaDao = db.puntoRutaDao();
        this.operacionDao = db.operacionPendienteDao();
        this.sessionManager = new SessionManager(this.context);
    }

    private ApiService getApiService() {
        return RetrofitClient.getService(context);
    }

    public void reportarIncidencia(int idRuta, int idRutaPunto, String nombrePunto, String tipo, String descripcion, File fotoComprimida, ReportarCallback callback) {
        String fechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        IncidenciaEntity localEntity = new IncidenciaEntity();
        localEntity.setIdRuta(idRuta);
        localEntity.setIdRutaPunto(idRutaPunto);
        localEntity.setNombrePunto(nombrePunto);
        localEntity.setTipo(tipo);
        localEntity.setDescripcion(descripcion);
        localEntity.setEstado("REPORTADA");
        localEntity.setIdUsuario(sessionManager.getUsuarioId());
        localEntity.setNombreUsuario(sessionManager.getNombre());
        localEntity.setFechaRegistro(fechaHora);
        if (fotoComprimida != null) {
            localEntity.setFotoRutaLocal(fotoComprimida.getAbsolutePath());
        }

        if (!NetworkUtils.hayConexion(context)) {
            guardarOffline(idRuta, idRutaPunto, localEntity, fotoComprimida, callback);
            return;
        }

        RequestBody tipoBody = RequestBody.create(MediaType.parse("text/plain"), tipo);
        RequestBody descBody = RequestBody.create(MediaType.parse("text/plain"), descripcion);

        MultipartBody.Part fotoPart = null;
        if (fotoComprimida != null && fotoComprimida.exists()) {
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), fotoComprimida);
            fotoPart = MultipartBody.Part.createFormData("foto", fotoComprimida.getName(), fileBody);
        }

        getApiService().registrarIncidenciaMultipart(idRutaPunto, tipoBody, descBody, fotoPart).enqueue(new Callback<ApiResponse<IncidenciaEntity>>() {
            @Override
            public void onResponse(Call<ApiResponse<IncidenciaEntity>> call, Response<ApiResponse<IncidenciaEntity>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    IncidenciaEntity serverData = response.body().getData();
                    localEntity.setIdRemoto(serverData.getIdRemoto());
                    localEntity.setFotoUrlRemota(serverData.getFotoUrlRemota());
                    localEntity.setSincronizado(true);

                    executor.execute(() -> {
                        incidenciaDao.insertar(localEntity);
                        puntoRutaDao.marcarConIncidencia(idRutaPunto);
                    });

                    callback.onSuccess(localEntity, false);
                } else {
                    callback.onError("Error al registrar en servidor: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<IncidenciaEntity>> call, Throwable t) {
                Log.w(TAG, "Falla de red, guardando incidencia local: " + t.getMessage());
                guardarOffline(idRuta, idRutaPunto, localEntity, fotoComprimida, callback);
            }
        });
    }

    private void guardarOffline(int idRuta, int idRutaPunto, IncidenciaEntity entity, File foto, ReportarCallback callback) {
        executor.execute(() -> {
            entity.setSincronizado(false);
            long localId = incidenciaDao.insertar(entity);
            entity.setId((int) localId);
            puntoRutaDao.marcarConIncidencia(idRutaPunto);

            OperacionPendienteEntity op = new OperacionPendienteEntity();
            op.setUuid(UUID.randomUUID().toString());
            op.setTipoOperacion("REGISTRAR_INCIDENCIA");
            op.setIdRuta(idRuta);
            op.setIdRutaPunto(idRutaPunto);
            op.setPayloadJson(gson.toJson(entity));
            if (foto != null) {
                op.setRutaArchivoFoto(foto.getAbsolutePath());
            }
            op.setEstado("PENDIENTE");
            op.setFechaCreacion(entity.getFechaRegistro());
            op.setReintentos(0);

            operacionDao.insertar(op);
            SyncManager.encolarSincronizacion(context);

            callback.onSuccess(entity, true);
        });
    }

    public void obtenerIncidencias(int idRuta, IncidenciasCallback callback) {
        getApiService().obtenerIncidenciasRuta(idRuta).enqueue(new Callback<ApiResponse<IncidenciasResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<IncidenciasResponse>> call, Response<ApiResponse<IncidenciasResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<IncidenciaEntity> list = response.body().getData().getIncidencias();
                    executor.execute(() -> incidenciaDao.insertarTodas(list));
                    callback.onResult(list);
                } else {
                    obtenerLocales(idRuta, callback);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<IncidenciasResponse>> call, Throwable t) {
                obtenerLocales(idRuta, callback);
            }
        });
    }

    private void obtenerLocales(int idRuta, IncidenciasCallback callback) {
        executor.execute(() -> {
            List<IncidenciaEntity> list = incidenciaDao.obtenerPorRuta(idRuta);
            callback.onResult(list);
        });
    }

    public void actualizarEstado(int idIncidencia, String estado, String observacion, Callback<ApiResponse<IncidenciaEntity>> callback) {
        EstadoIncidenciaRequest req = new EstadoIncidenciaRequest(estado, observacion);
        getApiService().actualizarEstadoIncidencia(idIncidencia, req).enqueue(new Callback<ApiResponse<IncidenciaEntity>>() {
            @Override
            public void onResponse(Call<ApiResponse<IncidenciaEntity>> call, Response<ApiResponse<IncidenciaEntity>> response) {
                if (response.isSuccessful()) {
                    executor.execute(() -> incidenciaDao.actualizarEstado(idIncidencia, estado));
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<ApiResponse<IncidenciaEntity>> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }
}
