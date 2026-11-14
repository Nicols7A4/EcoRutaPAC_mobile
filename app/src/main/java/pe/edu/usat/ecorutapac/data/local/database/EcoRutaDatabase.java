package pe.edu.usat.ecorutapac.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import pe.edu.usat.ecorutapac.data.local.dao.IncidenciaDao;
import pe.edu.usat.ecorutapac.data.local.dao.OperacionPendienteDao;
import pe.edu.usat.ecorutapac.data.local.dao.PuntoRutaDao;
import pe.edu.usat.ecorutapac.data.local.dao.RutaDao;
import pe.edu.usat.ecorutapac.data.local.entity.IncidenciaEntity;
import pe.edu.usat.ecorutapac.data.local.entity.OperacionPendienteEntity;
import pe.edu.usat.ecorutapac.data.local.entity.PuntoRutaEntity;
import pe.edu.usat.ecorutapac.data.local.entity.RutaEntity;

@Database(
        entities = {
                RutaEntity.class,
                PuntoRutaEntity.class,
                IncidenciaEntity.class,
                OperacionPendienteEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class EcoRutaDatabase extends RoomDatabase {

    public abstract RutaDao rutaDao();
    public abstract PuntoRutaDao puntoRutaDao();
    public abstract IncidenciaDao incidenciaDao();
    public abstract OperacionPendienteDao operacionPendienteDao();

    private static volatile EcoRutaDatabase INSTANCE;

    public static EcoRutaDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (EcoRutaDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            EcoRutaDatabase.class,
                            "ecoruta.db"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
