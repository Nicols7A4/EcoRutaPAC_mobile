package pe.edu.usat.ecorutapac.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity(tableName = "rutas")
public class RutaEntity implements Serializable {
    @PrimaryKey
    private int id;

    private String codigo;
    private String fecha;
    private String turno;

    @SerializedName("id_operario")
    private int idOperario;

    @SerializedName("nombre_operario")
    private String nombreOperario;

    @SerializedName("id_vehiculo")
    private int idVehiculo;

    @SerializedName("placa_vehiculo")
    private String placaVehiculo;

    private String estado;

    @SerializedName("hora_inicio")
    private String horaInicio;

    @SerializedName("hora_fin")
    private String horaFin;

    @SerializedName("total_puntos")
    private int totalPuntos;

    @SerializedName("puntos_atendidos")
    private int puntosAtendidos;

    @SerializedName("porcentaje_avance")
    private int porcentajeAvance;

    private boolean sincronizado;

    public RutaEntity() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }

    public int getIdOperario() { return idOperario; }
    public void setIdOperario(int idOperario) { this.idOperario = idOperario; }

    public String getNombreOperario() { return nombreOperario; }
    public void setNombreOperario(String nombreOperario) { this.nombreOperario = nombreOperario; }

    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }

    public String getPlacaVehiculo() { return placaVehiculo; }
    public void setPlacaVehiculo(String placaVehiculo) { this.placaVehiculo = placaVehiculo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFin() { return horaFin; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }

    public int getTotalPuntos() { return totalPuntos; }
    public void setTotalPuntos(int totalPuntos) { this.totalPuntos = totalPuntos; }

    public int getPuntosAtendidos() { return puntosAtendidos; }
    public void setPuntosAtendidos(int puntosAtendidos) { this.puntosAtendidos = puntosAtendidos; }

    public int getPorcentajeAvance() { return porcentajeAvance; }
    public void setPorcentajeAvance(int porcentajeAvance) { this.porcentajeAvance = porcentajeAvance; }

    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
}
