package pe.edu.usat.ecorutapac.sync;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SyncManager {

    private static final String UNIQUE_ONE_TIME_WORK = "ecoruta_sync_onetime";
    private static final String UNIQUE_PERIODIC_WORK = "ecoruta_sync_periodic";

    public static void encolarSincronizacion(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(
                        UNIQUE_ONE_TIME_WORK,
                        ExistingWorkPolicy.REPLACE,
                        syncRequest
                );
    }

    public static void iniciarSincronizacionPeriodica(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest periodicRequest = new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniquePeriodicWork(
                        UNIQUE_PERIODIC_WORK,
                        ExistingPeriodicWorkPolicy.KEEP,
                        periodicRequest
                );
    }

    public static LiveData<List<WorkInfo>> getWorkInfoLiveData(Context context) {
        return WorkManager.getInstance(context.getApplicationContext())
                .getWorkInfosForUniqueWorkLiveData(UNIQUE_ONE_TIME_WORK);
    }
}
