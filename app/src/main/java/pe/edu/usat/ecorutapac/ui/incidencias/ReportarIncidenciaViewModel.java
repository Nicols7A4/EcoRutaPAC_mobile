package pe.edu.usat.ecorutapac.ui.incidencias;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;

import pe.edu.usat.ecorutapac.data.local.entity.IncidenciaEntity;
import pe.edu.usat.ecorutapac.data.repository.IncidenciaRepository;
import pe.edu.usat.ecorutapac.ui.common.UiState;

public class ReportarIncidenciaViewModel extends AndroidViewModel {

    private final IncidenciaRepository repository;
    private final MutableLiveData<UiState<IncidenciaEntity>> reporteState = new MutableLiveData<>();

    public ReportarIncidenciaViewModel(@NonNull Application application) {
        super(application);
        this.repository = new IncidenciaRepository(application);
    }

    public LiveData<UiState<IncidenciaEntity>> getReporteState() {
        return reporteState;
    }

    public void enviarIncidencia(int idRuta, int idRutaPunto, String nombrePunto, String tipo, String desc, File fotoComprimida) {
        if (desc == null || desc.trim().isEmpty()) {
            reporteState.setValue(UiState.error("La descripción es obligatoria"));
            return;
        }

        reporteState.setValue(UiState.loading());

        repository.reportarIncidencia(idRuta, idRutaPunto, nombrePunto, tipo, desc.trim(), fotoComprimida, new IncidenciaRepository.ReportarCallback() {
            @Override
            public void onSuccess(IncidenciaEntity incidencia, boolean fueOffline) {
                reporteState.postValue(UiState.content(incidencia, fueOffline));
            }

            @Override
            public void onError(String mensaje) {
                reporteState.postValue(UiState.error(mensaje));
            }
        });
    }
}
