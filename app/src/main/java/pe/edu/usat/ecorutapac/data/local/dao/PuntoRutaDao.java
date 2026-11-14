package pe.edu.usat.ecorutapac.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import pe.edu.usat.ecorutapac.data.local.entity.PuntoRutaEntity;

@Dao
public interface PuntoRutaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarOActualizar(PuntoRutaEntity punto);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarTodos(List<PuntoRutaEntity> puntos);

    @Update
    void actualizar(PuntoRutaEntity punto);

    @Query("SELECT * FROM ruta_puntos WHERE idRuta = :idRuta ORDER BY orden ASC")
    List<PuntoRutaEntity> obtenerPuntosPorRuta(int idRuta);

    @Query("SELECT * FROM ruta_puntos WHERE idRuta = :idRuta ORDER BY orden ASC")
    LiveData<List<PuntoRutaEntity>> observarPuntosPorRuta(int idRuta);

    @Query("SELECT * FROM ruta_puntos WHERE id = :id LIMIT 1")
    PuntoRutaEntity obtenerPorId(int id);

    @Query("SELECT * FROM ruta_puntos WHERE codigoQr = :codigoQr AND idRuta = :idRuta LIMIT 1")
    PuntoRutaEntity obtenerPorQrYRuta(String codigoQr, int idRuta);

    @Query("UPDATE ruta_puntos SET estado = :estado, fechaAtencion = :fecha, latRegistro = :lat, lonRegistro = :lon, observacion = :obs WHERE id = :id")
    void marcarAtendido(int id, String estado, String fecha, Double lat, Double lon, String obs);

    @Query("UPDATE ruta_puntos SET estado = 'CON_INCIDENCIA' WHERE id = :id")
    void marcarConIncidencia(int id);

    @Query("SELECT COUNT(*) FROM ruta_puntos WHERE idRuta = :idRuta AND estado = 'ATENDIDO'")
    int contarAtendidosPorRuta(int idRuta);

    @Query("SELECT COUNT(*) FROM ruta_puntos WHERE idRuta = :idRuta")
    int contarTotalPorRuta(int idRuta);

    @Query("DELETE FROM ruta_puntos WHERE idRuta = :idRuta")
    void eliminarPorRuta(int idRuta);
}
