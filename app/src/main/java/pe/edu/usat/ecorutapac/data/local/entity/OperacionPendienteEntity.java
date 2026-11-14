package pe.edu.usat.ecorutapac.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "operaciones_pendientes")
public class OperacionPendienteEntity implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String uuid; // Identificador único para idempotencia
    private String tipoOperacion; // ATENDER_PUNTO, REGISTRAR_INCIDENCIA, INICIAR_RUTA, FINALIZAR_RUTA
    private int idRuta;
    private int idRutaPunto;
    private String payloadJson;
    private String rutaArchivoFoto;
    private String estado; // PENDIENTE, SINCRONIZADO, FALLIDO
    private int reintentos;
    private String fechaCreacion;
    private String ultimoError;

    public OperacionPendienteEntity() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(String tipoOperacion) { this.tipoOperacion = tipoOperacion; }

    public int getIdRuta() { return idRuta; }
    public void setIdRuta(int idRuta) { this.idRuta = idRuta; }

    public int getIdRutaPunto() { return idRutaPunto; }
    public void setIdRutaPunto(int idRutaPunto) { this.idRutaPunto = idRutaPunto; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public String getRutaArchivoFoto() { return rutaArchivoFoto; }
    public void setRutaArchivoFoto(String rutaArchivoFoto) { this.rutaArchivoFoto = rutaArchivoFoto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getReintentos() { return reintentos; }
    public void setReintentos(int reintentos) { this.reintentos = reintentos; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getUltimoError() { return ultimoError; }
    public void setUltimoError(String ultimoError) { this.ultimoError = ultimoError; }
}
