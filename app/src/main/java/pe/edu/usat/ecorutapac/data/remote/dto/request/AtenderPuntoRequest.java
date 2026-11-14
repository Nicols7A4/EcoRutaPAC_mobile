package pe.edu.usat.ecorutapac.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class AtenderPuntoRequest {
    @SerializedName("codigo_qr")
    private String codigoQr;

    private double latitud;
    private double longitud;
    private String observacion;

    @SerializedName("uuid_operacion")
    private String uuidOperacion;

    public AtenderPuntoRequest() {}

    public AtenderPuntoRequest(String codigoQr, double latitud, double longitud, String observacion, String uuidOperacion) {
        this.codigoQr = codigoQr;
        this.latitud = latitud;
        this.longitud = longitud;
        this.observacion = observacion;
        this.uuidOperacion = uuidOperacion;
    }

    public String getCodigoQr() { return codigoQr; }
    public void setCodigoQr(String codigoQr) { this.codigoQr = codigoQr; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public String getUuidOperacion() { return uuidOperacion; }
    public void setUuidOperacion(String uuidOperacion) { this.uuidOperacion = uuidOperacion; }
}
