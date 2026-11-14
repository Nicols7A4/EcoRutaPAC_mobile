package pe.edu.usat.ecorutapac.data.repository;

import android.content.Context;

import java.util.List;

import pe.edu.usat.ecorutapac.data.remote.RetrofitClient;
import pe.edu.usat.ecorutapac.data.remote.api.ApiService;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ApiResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.HistorialRutaItem;
import pe.edu.usat.ecorutapac.data.remote.dto.response.HistorialRutaResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.OperarioItem;
import pe.edu.usat.ecorutapac.data.remote.dto.response.OperariosResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ResumenSupervisionResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.VehiculoItem;
import pe.edu.usat.ecorutapac.data.remote.dto.response.VehiculosResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SupervisionRepository {

    private final Context context;

    public SupervisionRepository(Context context) {
        this.context = context.getApplicationContext();
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

    public void obtenerResumen(String fecha, Callback<ApiResponse<ResumenSupervisionResponse>> callback) {
        getApiService().obtenerResumenSupervision(fecha).enqueue(wrapCallback(callback));
    }

    public void obtenerOperarios(Callback<ApiResponse<OperariosResponse>> callback) {
        getApiService().obtenerOperarios().enqueue(wrapCallback(callback));
    }

    public void obtenerVehiculos(Callback<ApiResponse<VehiculosResponse>> callback) {
        getApiService().obtenerVehiculos().enqueue(wrapCallback(callback));
    }

    public void obtenerHistorialRuta(int idRuta, Callback<ApiResponse<HistorialRutaResponse>> callback) {
        getApiService().obtenerHistorialRuta(idRuta).enqueue(wrapCallback(callback));
    }
}
