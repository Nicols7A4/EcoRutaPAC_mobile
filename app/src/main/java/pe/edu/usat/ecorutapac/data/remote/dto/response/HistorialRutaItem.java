package pe.edu.usat.ecorutapac.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class HistorialRutaItem {
    private int id;
    @SerializedName("id_ruta")
    private int idRuta;
    @SerializedName("estado_anterior")
    private String estadoAnterior;
    @SerializedName("estado_nuevo")
    private String estadoNuevo;
    private String comentario;
    @SerializedName("id_usuario")
    private int idUsuario;
    @SerializedName("nombre_usuario")
    private String nombreUsuario;
    private String fecha;

    public HistorialRutaItem() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdRuta() { return idRuta; }
    public void setIdRuta(int idRuta) { this.idRuta = idRuta; }

    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}
