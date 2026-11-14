package pe.edu.usat.ecorutapac;

import android.app.Application;

import pe.edu.usat.ecorutapac.sync.SyncManager;

public class EcoRutaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Inicializar sincronización periódica en segundo plano
        SyncManager.iniciarSincronizacionPeriodica(this);
    }
}
