package pe.edu.usat.ecorutapac.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class DispositivoRequest {
    @SerializedName("fcm_token")
    private String fcmToken;

    @SerializedName("nombre_dispositivo")
    private String nombreDispositivo;

    private String plataforma;

    public DispositivoRequest() {}

    public DispositivoRequest(String fcmToken, String nombreDispositivo) {
        this.fcmToken = fcmToken;
        this.nombreDispositivo = nombreDispositivo;
        this.plataforma = "ANDROID";
    }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public String getNombreDispositivo() { return nombreDispositivo; }
    public void setNombreDispositivo(String nombreDispositivo) { this.nombreDispositivo = nombreDispositivo; }

    public String getPlataforma() { return plataforma; }
    public void setPlataforma(String plataforma) { this.plataforma = plataforma; }
}
