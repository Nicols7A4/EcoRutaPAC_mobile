package pe.edu.usat.ecorutapac.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import pe.edu.usat.ecorutapac.data.local.entity.OperacionPendienteEntity;

@Dao
public interface OperacionPendienteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertar(OperacionPendienteEntity operacion);

    @Update
    void actualizar(OperacionPendienteEntity operacion);

    @Delete
    void eliminar(OperacionPendienteEntity operacion);

    @Query("SELECT * FROM operaciones_pendientes WHERE estado = 'PENDIENTE' ORDER BY id ASC")
    List<OperacionPendienteEntity> obtenerPendientes();

    @Query("SELECT * FROM operaciones_pendientes WHERE uuid = :uuid LIMIT 1")
    OperacionPendienteEntity obtenerPorUuid(String uuid);

    @Query("SELECT COUNT(*) FROM operaciones_pendientes WHERE estado = 'PENDIENTE'")
    int contarPendientes();

    @Query("SELECT COUNT(*) FROM operaciones_pendientes WHERE estado = 'PENDIENTE'")
    LiveData<Integer> observarTotalPendientes();

    @Query("UPDATE operaciones_pendientes SET estado = 'SINCRONIZADO' WHERE id = :id")
    void marcarSincronizado(int id);

    @Query("UPDATE operaciones_pendientes SET reintentos = reintentos + 1, ultimoError = :error WHERE id = :id")
    void registrarFallo(int id, String error);
}
