package pe.edu.usat.ecorutapac.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import pe.edu.usat.ecorutapac.data.local.entity.RutaEntity;

public class RutasResponse {
    @SerializedName("rutas")
    private List<RutaEntity> rutas;

    public List<RutaEntity> getRutas() { return rutas; }
    public void setRutas(List<RutaEntity> rutas) { this.rutas = rutas; }
}
