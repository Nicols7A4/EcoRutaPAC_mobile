package pe.edu.usat.ecorutapac.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import pe.edu.usat.ecorutapac.data.local.entity.PuntoRutaEntity;

public class PuntosResponse {
    @SerializedName("id_ruta")
    private int idRuta;

    @SerializedName("puntos")
    private List<PuntoRutaEntity> puntos;

    public int getIdRuta() { return idRuta; }
    public List<PuntoRutaEntity> getPuntos() { return puntos; }
    public void setPuntos(List<PuntoRutaEntity> puntos) { this.puntos = puntos; }
}
