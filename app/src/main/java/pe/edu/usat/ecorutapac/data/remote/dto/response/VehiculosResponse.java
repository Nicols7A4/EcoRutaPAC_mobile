package pe.edu.usat.ecorutapac.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VehiculosResponse {
    @SerializedName("vehiculos")
    private List<VehiculoItem> vehiculos;

    public List<VehiculoItem> getVehiculos() { return vehiculos; }
    public void setVehiculos(List<VehiculoItem> vehiculos) { this.vehiculos = vehiculos; }
}
