package pe.edu.usat.ecorutapac.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity(tableName = "ruta_puntos")
public class PuntoRutaEntity implements Serializable {
    @PrimaryKey
    private int id; // ID de la tabla intermedia ruta_puntos

    @SerializedName("id_ruta")
    private int idRuta;

    @SerializedName("id_punto")
    private int idPunto;

    @SerializedName("codigo_qr")
    private String codigoQr;

    private String nombre;
    private String direccion;
    private double latitud;
    private double longitud;

    @SerializedName("radio_validacion")
    private int radioValidacion; // En metros (ej. 50m)

    private int orden;
    private String estado; // PENDIENTE, ATENDIDO, CON_INCIDENCIA

    @SerializedName("fecha_atencion")
    private String fechaAtencion;

    @SerializedName("lat_registro")
    private Double latRegistro;

    @SerializedName("lon_registro")
    private Double lonRegistro;

    private String observacion;

    public PuntoRutaEntity() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdRuta() { return idRuta; }
    public void setIdRuta(int idRuta) { this.idRuta = idRuta; }

    public int getIdPunto() { return idPunto; }
    public void setIdPunto(int idPunto) { this.idPunto = idPunto; }

    public String getCodigoQr() { return codigoQr; }
    public void setCodigoQr(String codigoQr) { this.codigoQr = codigoQr; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public int getRadioValidacion() { return radioValidacion; }
    public void setRadioValidacion(int radioValidacion) { this.radioValidacion = radioValidacion; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFechaAtencion() { return fechaAtencion; }
    public void setFechaAtencion(String fechaAtencion) { this.fechaAtencion = fechaAtencion; }

    public Double getLatRegistro() { return latRegistro; }
    public void setLatRegistro(Double latRegistro) { this.latRegistro = latRegistro; }

    public Double getLonRegistro() { return lonRegistro; }
    public void setLonRegistro(Double lonRegistro) { this.lonRegistro = lonRegistro; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
