package pe.edu.usat.ecorutapac.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HistorialRutaResponse {
    @SerializedName("historial")
    private List<HistorialRutaItem> historial;

    public List<HistorialRutaItem> getHistorial() { return historial; }
    public void setHistorial(List<HistorialRutaItem> historial) { this.historial = historial; }
}
