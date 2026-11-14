package pe.edu.usat.ecorutapac.data.remote.dto.request;

public class EstadoIncidenciaRequest {
    private String estado;
    private String observacion;

    public EstadoIncidenciaRequest() {}

    public EstadoIncidenciaRequest(String estado, String observacion) {
        this.estado = estado;
        this.observacion = observacion;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
