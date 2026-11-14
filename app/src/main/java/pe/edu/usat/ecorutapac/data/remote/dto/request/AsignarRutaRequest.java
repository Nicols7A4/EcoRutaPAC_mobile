package pe.edu.usat.ecorutapac.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class AsignarRutaRequest {
    @SerializedName("id_operario")
    private int idOperario;

    @SerializedName("id_vehiculo")
    private int idVehiculo;

    public AsignarRutaRequest() {}

    public AsignarRutaRequest(int idOperario, int idVehiculo) {
        this.idOperario = idOperario;
        this.idVehiculo = idVehiculo;
    }

    public int getIdOperario() { return idOperario; }
    public void setIdOperario(int idOperario) { this.idOperario = idOperario; }

    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }
}
