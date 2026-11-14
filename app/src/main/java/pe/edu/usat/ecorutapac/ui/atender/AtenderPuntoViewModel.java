package pe.edu.usat.ecorutapac.ui.atender;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import pe.edu.usat.ecorutapac.data.local.entity.PuntoRutaEntity;
import pe.edu.usat.ecorutapac.data.repository.RutaRepository;
import pe.edu.usat.ecorutapac.ui.common.UiState;

public class AtenderPuntoViewModel extends AndroidViewModel {

    private final RutaRepository rutaRepository;
    private final MutableLiveData<UiState<PuntoRutaEntity>> atencionState = new MutableLiveData<>();

    public AtenderPuntoViewModel(@NonNull Application application) {
        super(application);
        this.rutaRepository = new RutaRepository(application);
    }

    public LiveData<UiState<PuntoRutaEntity>> getAtencionState() {
        return atencionState;
    }

    public void confirmarAtencion(int idRuta, int idRutaPunto, String qr, double lat, double lon, String obs) {
        atencionState.setValue(UiState.loading());

        rutaRepository.atenderPunto(idRuta, idRutaPunto, qr, lat, lon, obs, new RutaRepository.AtencionCallback() {
            @Override
            public void onSuccess(PuntoRutaEntity punto, boolean fueOffline) {
                atencionState.postValue(UiState.content(punto, fueOffline));
            }

            @Override
            public void onError(String mensaje) {
                atencionState.postValue(UiState.error(mensaje));
            }
        });
    }
}
