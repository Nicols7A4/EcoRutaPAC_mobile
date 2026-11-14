package pe.edu.usat.ecorutapac.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class ResumenSupervisionResponse {
    @SerializedName("total_rutas")
    private int totalRutas;

    @SerializedName("rutas_programadas")
    private int rutasProgramadas;

    @SerializedName("rutas_en_curso")
    private int rutasEnCurso;

    @SerializedName("rutas_finalizadas")
    private int rutasFinalizadas;

    @SerializedName("rutas_observadas")
    private int rutasObservadas;

    @SerializedName("total_puntos")
    private int totalPuntos;

    @SerializedName("puntos_atendidos")
    private int puntosAtendidos;

    @SerializedName("total_incidencias")
    private int totalIncidencias;

    public ResumenSupervisionResponse() {}

    public int getTotalRutas() { return totalRutas; }
    public void setTotalRutas(int totalRutas) { this.totalRutas = totalRutas; }

    public int getRutasProgramadas() { return rutasProgramadas; }
    public void setRutasProgramadas(int rutasProgramadas) { this.rutasProgramadas = rutasProgramadas; }

    public int getRutasEnCurso() { return rutasEnCurso; }
    public void setRutasEnCurso(int rutasEnCurso) { this.rutasEnCurso = rutasEnCurso; }

    public int getRutasFinalizadas() { return rutasFinalizadas; }
    public void setRutasFinalizadas(int rutasFinalizadas) { this.rutasFinalizadas = rutasFinalizadas; }

    public int getRutasObservadas() { return rutasObservadas; }
    public void setRutasObservadas(int rutasObservadas) { this.rutasObservadas = rutasObservadas; }

    public int getTotalPuntos() { return totalPuntos; }
    public void setTotalPuntos(int totalPuntos) { this.totalPuntos = totalPuntos; }

    public int getPuntosAtendidos() { return puntosAtendidos; }
    public void setPuntosAtendidos(int puntosAtendidos) { this.puntosAtendidos = puntosAtendidos; }

    public int getTotalIncidencias() { return totalIncidencias; }
    public void setTotalIncidencias(int totalIncidencias) { this.totalIncidencias = totalIncidencias; }
}
