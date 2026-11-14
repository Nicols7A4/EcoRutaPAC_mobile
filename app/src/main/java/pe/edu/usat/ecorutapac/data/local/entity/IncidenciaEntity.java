package pe.edu.usat.ecorutapac.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity(tableName = "incidencias")
public class IncidenciaEntity implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @SerializedName("id_remoto")
    private Integer idRemoto;

    @SerializedName("id_ruta_punto")
    private int idRutaPunto;

    @SerializedName("id_ruta")
    private int idRuta;

    @SerializedName("nombre_punto")
    private String nombrePunto;

    private String tipo;
    private String descripcion;
    private String estado; // REPORTADA, REVISADA, RESUELTA

    @SerializedName("id_usuario")
    private int idUsuario;

    @SerializedName("nombre_usuario")
    private String nombreUsuario;

    @SerializedName("fecha_registro")
    private String fechaRegistro;

    @SerializedName("foto_ruta_local")
    private String fotoRutaLocal;

    @SerializedName("foto_url_remota")
    private String fotoUrlRemota;

    private boolean sincronizado;

    public IncidenciaEntity() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getIdRemoto() { return idRemoto; }
    public void setIdRemoto(Integer idRemoto) { this.idRemoto = idRemoto; }

    public int getIdRutaPunto() { return idRutaPunto; }
    public void setIdRutaPunto(int idRutaPunto) { this.idRutaPunto = idRutaPunto; }

    public int getIdRuta() { return idRuta; }
    public void setIdRuta(int idRuta) { this.idRuta = idRuta; }

    public String getNombrePunto() { return nombrePunto; }
    public void setNombrePunto(String nombrePunto) { this.nombrePunto = nombrePunto; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getFotoRutaLocal() { return fotoRutaLocal; }
    public void setFotoRutaLocal(String fotoRutaLocal) { this.fotoRutaLocal = fotoRutaLocal; }

    public String getFotoUrlRemota() { return fotoUrlRemota; }
    public void setFotoUrlRemota(String fotoUrlRemota) { this.fotoUrlRemota = fotoUrlRemota; }

    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
}
