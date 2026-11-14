package pe.edu.usat.ecorutapac.sync;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pe.edu.usat.ecorutapac.data.local.dao.IncidenciaDao;
import pe.edu.usat.ecorutapac.data.local.dao.OperacionPendienteDao;
import pe.edu.usat.ecorutapac.data.local.dao.PuntoRutaDao;
import pe.edu.usat.ecorutapac.data.local.dao.RutaDao;
import pe.edu.usat.ecorutapac.data.local.database.EcoRutaDatabase;
import pe.edu.usat.ecorutapac.data.local.entity.IncidenciaEntity;
import pe.edu.usat.ecorutapac.data.local.entity.OperacionPendienteEntity;
import pe.edu.usat.ecorutapac.data.local.entity.PuntoRutaEntity;
import pe.edu.usat.ecorutapac.data.remote.RetrofitClient;
import pe.edu.usat.ecorutapac.data.remote.api.ApiService;
import pe.edu.usat.ecorutapac.data.remote.dto.request.AtenderPuntoRequest;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ApiResponse;
import retrofit2.Response;

public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";
    private final Gson gson = new Gson();

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        EcoRutaDatabase db = EcoRutaDatabase.getInstance(context);
        OperacionPendienteDao operacionDao = db.operacionPendienteDao();
        PuntoRutaDao puntoRutaDao = db.puntoRutaDao();
        RutaDao rutaDao = db.rutaDao();
        IncidenciaDao incidenciaDao = db.incidenciaDao();
        ApiService apiService = RetrofitClient.getService(context);

        List<OperacionPendienteEntity> pendientes = operacionDao.obtenerPendientes();
        if (pendientes == null || pendientes.isEmpty()) {
            Log.d(TAG, "No hay operaciones pendientes para sincronizar");
            return Result.success();
        }

        boolean huboErrorReintentable = false;

        for (OperacionPendienteEntity op : pendientes) {
            try {
                if ("ATENDER_PUNTO".equalsIgnoreCase(op.getTipoOperacion())) {
                    AtenderPuntoRequest request = gson.fromJson(op.getPayloadJson(), AtenderPuntoRequest.class);
                    // Garantizar idempotencia enviando el UUID único
                    request.setUuidOperacion(op.getUuid());

                    Response<ApiResponse<PuntoRutaEntity>> response = apiService.atenderPunto(op.getIdRutaPunto(), request).execute();

                    if (response.isSuccessful() || response.code() == 409) {
                        // 200/201 éxito, o 409 ya atendido previamente (idempotencia)
                        operacionDao.marcarSincronizado(op.getId());
                        puntoRutaDao.marcarAtendido(
                                op.getIdRutaPunto(),
                                "ATENDIDO",
                                op.getFechaCreacion(),
                                request.getLatitud(),
                                request.getLongitud(),
                                request.getObservacion()
                        );
                        // Actualizar avance local
                        int atendidos = puntoRutaDao.contarAtendidosPorRuta(op.getIdRuta());
                        int total = puntoRutaDao.contarTotalPorRuta(op.getIdRuta());
                        int porcentaje = total > 0 ? (atendidos * 100 / total) : 0;
                        rutaDao.actualizarAvance(op.getIdRuta(), atendidos, porcentaje);
                        Log.d(TAG, "Atención sincronizada con éxito para punto ID: " + op.getIdRutaPunto());
                    } else if (response.code() >= 500 || response.code() == 408) {
                        huboErrorReintentable = true;
                        operacionDao.registrarFallo(op.getId(), "HTTP " + response.code());
                    } else {
                        // Error de cliente irrecuperable
                        operacionDao.registrarFallo(op.getId(), "HTTP Error definitivo " + response.code());
                    }

                } else if ("REGISTRAR_INCIDENCIA".equalsIgnoreCase(op.getTipoOperacion())) {
                    IncidenciaEntity inc = gson.fromJson(op.getPayloadJson(), IncidenciaEntity.class);

                    RequestBody tipoPart = RequestBody.create(MediaType.parse("text/plain"), inc.getTipo());
                    RequestBody descPart = RequestBody.create(MediaType.parse("text/plain"), inc.getDescripcion());

                    MultipartBody.Part fotoPart = null;
                    if (!TextUtils.isEmpty(op.getRutaArchivoFoto())) {
                        File fotoFile = new File(op.getRutaArchivoFoto());
                        if (fotoFile.exists()) {
                            RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), fotoFile);
                            fotoPart = MultipartBody.Part.createFormData("foto", fotoFile.getName(), fileBody);
                        }
                    }

                    Response<ApiResponse<IncidenciaEntity>> response = apiService.registrarIncidenciaMultipart(
                            op.getIdRutaPunto(),
                            tipoPart,
                            descPart,
                            fotoPart
                    ).execute();

                    if (response.isSuccessful() || response.code() == 409) {
                        operacionDao.marcarSincronizado(op.getId());
                        puntoRutaDao.marcarConIncidencia(op.getIdRutaPunto());
                        if (response.body() != null && response.body().getData() != null) {
                            inc.setIdRemoto(response.body().getData().getIdRemoto());
                        }
                        inc.setSincronizado(true);
                        incidenciaDao.insertar(inc);
                        Log.d(TAG, "Incidencia sincronizada con éxito");
                    } else if (response.code() >= 500 || response.code() == 408) {
                        huboErrorReintentable = true;
                        operacionDao.registrarFallo(op.getId(), "HTTP " + response.code());
                    } else {
                        operacionDao.registrarFallo(op.getId(), "HTTP Definitivo " + response.code());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Excepción de red al sincronizar operación " + op.getId(), e);
                huboErrorReintentable = true;
                operacionDao.registrarFallo(op.getId(), e.getMessage());
            }
        }

        if (huboErrorReintentable) {
            return Result.retry();
        }
        return Result.success();
    }
}
