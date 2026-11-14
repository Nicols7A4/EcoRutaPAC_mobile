package pe.edu.usat.ecorutapac.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import pe.edu.usat.ecorutapac.data.local.entity.IncidenciaEntity;

public class IncidenciasResponse {
    @SerializedName("incidencias")
    private List<IncidenciaEntity> incidencias;

    public List<IncidenciaEntity> getIncidencias() { return incidencias; }
    public void setIncidencias(List<IncidenciaEntity> incidencias) { this.incidencias = incidencias; }
}
