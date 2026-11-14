package pe.edu.usat.ecorutapac.data.remote.api;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pe.edu.usat.ecorutapac.data.local.entity.IncidenciaEntity;
import pe.edu.usat.ecorutapac.data.local.entity.PuntoRutaEntity;
import pe.edu.usat.ecorutapac.data.local.entity.RutaEntity;
import pe.edu.usat.ecorutapac.data.remote.dto.request.AsignarRutaRequest;
import pe.edu.usat.ecorutapac.data.remote.dto.request.AtenderPuntoRequest;
import pe.edu.usat.ecorutapac.data.remote.dto.request.DispositivoRequest;
import pe.edu.usat.ecorutapac.data.remote.dto.request.EstadoIncidenciaRequest;
import pe.edu.usat.ecorutapac.data.remote.dto.request.LoginRequest;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ApiResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.HistorialRutaItem;
import pe.edu.usat.ecorutapac.data.remote.dto.response.HistorialRutaResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.IncidenciasResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.LoginResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.OperarioItem;
import pe.edu.usat.ecorutapac.data.remote.dto.response.OperariosResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.PuntosResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ResumenSupervisionResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.RutasResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.VehiculoItem;
import pe.edu.usat.ecorutapac.data.remote.dto.response.VehiculosResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // --- Módulo 1: Autenticación y FCM ---
    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);

    @POST("auth/fcm-token")
    Call<ApiResponse<Object>> registrarTokenFcm(@Body DispositivoRequest request);

    @GET("usuario/perfil")
    Call<ApiResponse<LoginResponse>> obtenerPerfil();

    // --- Módulo 2: Rutas ---
    @GET("rutas")
    Call<ApiResponse<RutasResponse>> obtenerRutas(
            @Query("fecha") String fecha,
            @Query("estado") String estado,
            @Query("id_operario") Integer idOperario
    );

    @GET("rutas/{id}")
    Call<ApiResponse<RutaEntity>> obtenerDetalleRuta(@Path("id") int idRuta);

    @GET("rutas/{id}/puntos")
    Call<ApiResponse<PuntosResponse>> obtenerPuntosRuta(@Path("id") int idRuta);

    @PUT("rutas/{id}/asignar")
    Call<ApiResponse<RutaEntity>> asignarRuta(@Path("id") int idRuta, @Body AsignarRutaRequest request);

    @POST("rutas/{id}/iniciar")
    Call<ApiResponse<RutaEntity>> iniciarRuta(@Path("id") int idRuta);

    @POST("rutas/{id}/finalizar")
    Call<ApiResponse<RutaEntity>> finalizarRuta(@Path("id") int idRuta);

    // --- Módulo 3: Puntos y Atención ---
    @GET("puntos/{codigo_qr}")
    Call<ApiResponse<PuntoRutaEntity>> obtenerPuntoPorQr(@Path("codigo_qr") String codigoQr);

    @POST("ruta-puntos/{id}/atender")
    Call<ApiResponse<PuntoRutaEntity>> atenderPunto(@Path("id") int idRutaPunto, @Body AtenderPuntoRequest request);

    // --- Módulo 4: Incidencias y Evidencias ---
    @Multipart
    @POST("ruta-puntos/{id}/incidencias")
    Call<ApiResponse<IncidenciaEntity>> registrarIncidenciaMultipart(
            @Path("id") int idRutaPunto,
            @Part("tipo") RequestBody tipo,
            @Part("descripcion") RequestBody descripcion,
            @Part MultipartBody.Part foto
    );

    @GET("rutas/{id}/incidencias")
    Call<ApiResponse<IncidenciasResponse>> obtenerIncidenciasRuta(@Path("id") int idRuta);

    @PUT("incidencias/{id}/estado")
    Call<ApiResponse<IncidenciaEntity>> actualizarEstadoIncidencia(
            @Path("id") int idIncidencia,
            @Body EstadoIncidenciaRequest request
    );

    // --- Supervisión e Historial ---
    @GET("rutas/{id}/historial")
    Call<ApiResponse<HistorialRutaResponse>> obtenerHistorialRuta(@Path("id") int idRuta);

    @GET("supervision/resumen")
    Call<ApiResponse<ResumenSupervisionResponse>> obtenerResumenSupervision(@Query("fecha") String fecha);

    @GET("usuarios/operarios")
    Call<ApiResponse<OperariosResponse>> obtenerOperarios();

    @GET("vehiculos")
    Call<ApiResponse<VehiculosResponse>> obtenerVehiculos();
}
