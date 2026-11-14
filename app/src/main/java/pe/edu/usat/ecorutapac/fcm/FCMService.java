package pe.edu.usat.ecorutapac.fcm;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import pe.edu.usat.ecorutapac.data.remote.RetrofitClient;
import pe.edu.usat.ecorutapac.data.remote.dto.request.DispositivoRequest;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ApiResponse;
//import pe.edu.usat.ecorutapac.ui.incidencias.ListaIncidenciasActivity;
//import pe.edu.usat.ecorutapac.ui.main.MainActivity;
///import pe.edu.usat.ecorutapac.ui.rutas.DetalleRutaActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FCMService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nuevo FCM Token recibido: " + token);

        String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
        FirebaseTokenManager.setFirebaseToken(getApplicationContext(), token, deviceName);

        // Si ya hay usuario autenticado, enviar inmediatamente a la API
        DispositivoRequest request = new DispositivoRequest(token, deviceName);
        RetrofitClient.getService(getApplicationContext()).registrarTokenFcm(request).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Token FCM sincronizado con el backend exitosamente");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                Log.w(TAG, "No se pudo sincronizar token FCM: " + t.getMessage());
            }
        });
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String title = "EcoRuta Alerta";
        String body = "Tienes una actualización del servicio";

        if (message.getNotification() != null) {
            if (message.getNotification().getTitle() != null) {
                title = message.getNotification().getTitle();
            }
            if (message.getNotification().getBody() != null) {
                body = message.getNotification().getBody();
            }
        }

        Map<String, String> data = message.getData();
        if (data.containsKey("title")) title = data.get("title");
        if (data.containsKey("body")) body = data.get("body");


        /*
        // Reto R4: Manejo de Deep Link según el evento y parámetros del mensaje FCM
        Intent intent;
        String tipoEvento = data.get("tipo");
        String idRutaStr = data.get("id_ruta");

        if ("INCIDENCIA_CRITICA".equalsIgnoreCase(tipoEvento) || "INCIDENCIA".equalsIgnoreCase(tipoEvento)) {
            intent = new Intent(getApplicationContext(), ListaIncidenciasActivity.class);
            if (idRutaStr != null) {
                try {
                    intent.putExtra("id_ruta", Integer.parseInt(idRutaStr));
                } catch (NumberFormatException ignored) {}
            }
        } else if (idRutaStr != null && !idRutaStr.isEmpty()) {
            // Abrir directamente el detalle de la ruta asignada o modificada
            intent = new Intent(getApplicationContext(), DetalleRutaActivity.class);
            try {
                intent.putExtra("id_ruta", Integer.parseInt(idRutaStr));
            } catch (NumberFormatException ignored) {}
        } else {
            intent = new Intent(getApplicationContext(), MainActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        new NotificationUtils(getApplicationContext()).showNotificationMessage(title, body, intent);


         */
    }
}
