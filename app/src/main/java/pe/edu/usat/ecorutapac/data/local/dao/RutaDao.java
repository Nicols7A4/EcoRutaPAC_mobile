package pe.edu.usat.ecorutapac.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import pe.edu.usat.ecorutapac.data.local.entity.RutaEntity;

@Dao
public interface RutaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarOActualizar(RutaEntity ruta);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarTodas(List<RutaEntity> rutas);

    @Update
    void actualizar(RutaEntity ruta);

    @Query("SELECT * FROM rutas ORDER BY fecha DESC, id DESC")
    List<RutaEntity> obtenerTodas();

    @Query("SELECT * FROM rutas ORDER BY fecha DESC, id DESC")
    LiveData<List<RutaEntity>> observarTodas();

    @Query("SELECT * FROM rutas WHERE id = :id LIMIT 1")
    RutaEntity obtenerPorId(int id);

    @Query("SELECT * FROM rutas WHERE id = :id LIMIT 1")
    LiveData<RutaEntity> observarPorId(int id);

    @Query("SELECT * FROM rutas WHERE (:fecha IS NULL OR fecha = :fecha) AND (:estado IS NULL OR estado = :estado) AND (:idOperario <= 0 OR idOperario = :idOperario) ORDER BY fecha DESC, id DESC")
    List<RutaEntity> filtrarRutas(String fecha, String estado, int idOperario);

    @Query("UPDATE rutas SET estado = :nuevoEstado WHERE id = :idRuta")
    void actualizarEstado(int idRuta, String nuevoEstado);

    @Query("UPDATE rutas SET puntosAtendidos = :atendidos, porcentajeAvance = :porcentaje WHERE id = :idRuta")
    void actualizarAvance(int idRuta, int atendidos, int porcentaje);

    @Query("DELETE FROM rutas")
    void eliminarTodas();
}
