package pe.edu.usat.ecorutapac.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OperariosResponse {
    @SerializedName("operarios")
    private List<OperarioItem> operarios;

    public List<OperarioItem> getOperarios() { return operarios; }
    public void setOperarios(List<OperarioItem> operarios) { this.operarios = operarios; }
}
