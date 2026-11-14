package pe.edu.usat.ecorutapac.data.remote.dto.response;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private String token;
    private int id;
    private String nombre;
    private String correo;
    private String rol;

    public LoginResponse() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
