package pe.edu.usat.ecorutapac.ui.mapa;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pe.edu.usat.ecorutapac.R;
import pe.edu.usat.ecorutapac.data.local.entity.PuntoRutaEntity;
import pe.edu.usat.ecorutapac.data.remote.dto.response.ApiResponse;
import pe.edu.usat.ecorutapac.data.remote.dto.response.PuntosResponse;
import pe.edu.usat.ecorutapac.data.repository.RutaRepository;
import pe.edu.usat.ecorutapac.databinding.ActivityMapaRutaBinding;
import pe.edu.usat.ecorutapac.util.LocationHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaRutaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityMapaRutaBinding binding;
    private GoogleMap mMap;
    private RutaRepository rutaRepository;
    private LocationHelper locationHelper;
    private int idRuta;
    private Location ubicacionActual;
    private final Map<Marker, PuntoRutaEntity> markerMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapaRutaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        idRuta = getIntent().getIntExtra("id_ruta", 1);
        rutaRepository = new RutaRepository(this);
        locationHelper = new LocationHelper(this);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        obtenerUbicacionActual();
    }

    private void obtenerUbicacionActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationHelper.obtenerUbicacionActual(new LocationHelper.LocationResultCallback() {
                @Override
                public void onLocationObtained(Location location) {
                    ubicacionActual = location;
                    if (mMap != null) {
                        try {
                            mMap.setMyLocationEnabled(true);
                        } catch (SecurityException ignored) {}
                    }
                }

                @Override
                public void onError(String message) {}
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerClickListener(marker -> {
            PuntoRutaEntity p = markerMap.get(marker);
            if (p != null) {
                float dist = 0;
                if (ubicacionActual != null) {
                    dist = LocationHelper.calcularDistanciaMetros(ubicacionActual.getLatitude(), ubicacionActual.getLongitude(), p.getLatitud(), p.getLongitud());
                }
                binding.tvPuntoSeleccionadoInfo.setText(String.format(Locale.getDefault(),
                        "Seleccionado: %s\nEstado: %s | Distancia: %.1f m\nToque para navegar en Google Maps",
                        p.getNombre(), p.getEstado(), dist));
            }
            return false;
        });

        mMap.setOnInfoWindowClickListener(marker -> {
            PuntoRutaEntity p = markerMap.get(marker);
            if (p != null) {
                String uri = String.format(Locale.US, "google.navigation:q=%f,%f", p.getLatitud(), p.getLongitud());
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
            }
        });

        cargarPuntosEnMapa();
    }

    private void cargarPuntosEnMapa() {
        rutaRepository.obtenerPuntosRutaRemotos(idRuta, new Callback<ApiResponse<PuntosResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PuntosResponse>> call, Response<ApiResponse<PuntosResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    mostrarMarcadores(response.body().getData().getPuntos());
                } else {
                    cargarPuntosLocales();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PuntosResponse>> call, Throwable t) {
                cargarPuntosLocales();
            }
        });
    }

    private void cargarPuntosLocales() {
        rutaRepository.obtenerPuntosLocales(idRuta, puntos -> runOnUiThread(() -> {
            if (puntos != null && !puntos.isEmpty()) {
                mostrarMarcadores(puntos);
            }
        }));
    }

    private void mostrarMarcadores(List<PuntoRutaEntity> puntos) {
        if (mMap == null || puntos == null || puntos.isEmpty()) return;

        mMap.clear();
        markerMap.clear();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        for (PuntoRutaEntity p : puntos) {
            LatLng pos = new LatLng(p.getLatitud(), p.getLongitud());
            boundsBuilder.include(pos);

            float hue;
            String estado = p.getEstado() != null ? p.getEstado().toUpperCase() : "PENDIENTE";
            if ("ATENDIDO".equals(estado)) {
                hue = BitmapDescriptorFactory.HUE_GREEN;
            } else if ("CON_INCIDENCIA".equals(estado)) {
                hue = BitmapDescriptorFactory.HUE_RED;
            } else {
                hue = BitmapDescriptorFactory.HUE_ORANGE;
            }

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(p.getOrden() + ". " + p.getNombre())
                    .snippet("Estado: " + estado + " | QR: " + p.getCodigoQr())
                    .icon(BitmapDescriptorFactory.defaultMarker(hue)));

            if (marker != null) {
                markerMap.put(marker, p);
            }
        }

        try {
            LatLngBounds bounds = boundsBuilder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120));
        } catch (Exception e) {
            // Centro en el primer punto
            LatLng primera = new LatLng(puntos.get(0).getLatitud(), puntos.get(0).getLongitud());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(primera, 16f));
        }
    }
}
