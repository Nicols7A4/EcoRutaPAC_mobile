package pe.edu.usat.ecorutapac.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import pe.edu.usat.ecorutapac.data.local.entity.IncidenciaEntity;

@Dao
public interface IncidenciaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertar(IncidenciaEntity incidencia);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarTodas(List<IncidenciaEntity> incidencias);

    @Update
    void actualizar(IncidenciaEntity incidencia);

    @Query("SELECT * FROM incidencias WHERE idRuta = :idRuta ORDER BY id DESC")
    List<IncidenciaEntity> obtenerPorRuta(int idRuta);

    @Query("SELECT * FROM incidencias WHERE idRuta = :idRuta ORDER BY id DESC")
    LiveData<List<IncidenciaEntity>> observarPorRuta(int idRuta);

    @Query("SELECT * FROM incidencias WHERE id = :id LIMIT 1")
    IncidenciaEntity obtenerPorId(int id);

    @Query("UPDATE incidencias SET estado = :nuevoEstado WHERE id = :id")
    void actualizarEstado(int id, String nuevoEstado);

    @Query("DELETE FROM incidencias WHERE idRuta = :idRuta")
    void eliminarPorRuta(int idRuta);
}
