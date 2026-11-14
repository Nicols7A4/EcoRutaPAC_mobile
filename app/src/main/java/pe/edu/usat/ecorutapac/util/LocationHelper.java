package pe.edu.usat.ecorutapac.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationHelper {

    public interface LocationResultCallback {
        void onLocationObtained(Location location);
        void onError(String message);
    }

    private final Context context;
    private final FusedLocationProviderClient fusedClient;

    public LocationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.fusedClient = LocationServices.getFusedLocationProviderClient(this.context);
    }

    @SuppressLint("MissingPermission")
    public void obtenerUbicacionActual(@NonNull LocationResultCallback callback) {
        try {
            fusedClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    callback.onLocationObtained(location);
                } else {
                    // Solicitar actualización puntual si lastLocation es null
                    solicitarActualizacionDirecta(callback);
                }
            }).addOnFailureListener(e -> callback.onError("Error al obtener ubicación: " + e.getMessage()));
        } catch (Exception ex) {
            callback.onError("Excepción de permisos o hardware: " + ex.getMessage());
        }
    }

    @SuppressLint("MissingPermission")
    private void solicitarActualizacionDirecta(@NonNull LocationResultCallback callback) {
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMaxUpdates(1)
                .build();

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                fusedClient.removeLocationUpdates(this);
                if (locationResult.getLastLocation() != null) {
                    callback.onLocationObtained(locationResult.getLastLocation());
                } else {
                    callback.onError("No se pudo fijar la señal GPS del dispositivo.");
                }
            }
        };

        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
    }

    /**
     * Calcula la distancia en metros entre dos coordenadas geográficas.
     */
    public static float calcularDistanciaMetros(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    /**
     * Valida si el operario se encuentra dentro del radio permitido (ej. 50 metros).
     */
    public static boolean estaDentroDelRadio(double latActual, double lonActual, double latPunto, double lonPunto, int radioMaximoMetros) {
        float distancia = calcularDistanciaMetros(latActual, lonActual, latPunto, lonPunto);
        return distancia <= radioMaximoMetros;
    }
}
