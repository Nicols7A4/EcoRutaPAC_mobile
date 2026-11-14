package pe.edu.usat.ecorutapac;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import pe.edu.usat.ecorutapac.sync.SyncManager;

public class EcoRutaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Forzar modo claro en toda la aplicación
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Inicializar sincronización periódica en segundo plano
        SyncManager.iniciarSincronizacionPeriodica(this);
    }
}
