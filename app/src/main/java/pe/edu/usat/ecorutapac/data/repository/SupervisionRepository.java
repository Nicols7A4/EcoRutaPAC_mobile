package pe.edu.usat.ecorutapac.data.repository;

import android.content.Context;

import java.util.List;

import pe.edu.usat.ecorutapac.data.remote.RetrofitClient;
import pe.edu.usat.ecorutapac.data.remote.api.ApiService;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ApiResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.HistorialRutaItem;
import pe.edu.usat.ecorutapac.data.remote.dto.response.OperarioItem;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ResumenSupervisionResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.VehiculoItem;
import retrofit2.Callback;

public class SupervisionRepository {

    private final ApiService apiService;

    public SupervisionRepository(Context context) {
        this.apiService = RetrofitClient.getService(context.getApplicationContext());
    }

    public void obtenerResumen(String fecha, Callback<ApiResponse<ResumenSupervisionResponse>> callback) {
        apiService.obtenerResumenSupervision(fecha).enqueue(callback);
    }

    public void obtenerOperarios(Callback<ApiResponse<List<OperarioItem>>> callback) {
        apiService.obtenerOperarios().enqueue(callback);
    }

    public void obtenerVehiculos(Callback<ApiResponse<List<VehiculoItem>>> callback) {
        apiService.obtenerVehiculos().enqueue(callback);
    }

    public void obtenerHistorialRuta(int idRuta, Callback<ApiResponse<List<HistorialRutaItem>>> callback) {
        apiService.obtenerHistorialRuta(idRuta).enqueue(callback);
    }
}
