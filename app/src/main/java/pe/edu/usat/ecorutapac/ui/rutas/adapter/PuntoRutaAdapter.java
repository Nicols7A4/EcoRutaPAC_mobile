package pe.edu.usat.ecorutapac.ui.rutas.adapter;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pe.edu.usat.ecorutapac.R;
import pe.edu.usat.ecorutapac.data.local.entity.PuntoRutaEntity;
import pe.edu.usat.ecorutapac.util.LocationHelper;

public class PuntoRutaAdapter extends RecyclerView.Adapter<PuntoRutaAdapter.PuntoViewHolder> {

    public interface OnPuntoActionListener {
        void onAtenderClick(PuntoRutaEntity punto);
        void onIncidenciaClick(PuntoRutaEntity punto);
    }

    private List<PuntoRutaEntity> listaPuntos = new ArrayList<>();
    private Location ubicacionActual;
    private final OnPuntoActionListener listener;

    public PuntoRutaAdapter(OnPuntoActionListener listener) {
        this.listener = listener;
    }

    public void setPuntos(List<PuntoRutaEntity> puntos) {
        this.listaPuntos = puntos != null ? puntos : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setUbicacionActual(Location location) {
        this.ubicacionActual = location;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PuntoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_punto_ruta, parent, false);
        return new PuntoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PuntoViewHolder holder, int position) {
        PuntoRutaEntity punto = listaPuntos.get(position);
        holder.bind(punto, ubicacionActual, listener);
    }

    @Override
    public int getItemCount() {
        return listaPuntos.size();
    }

    static class PuntoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvOrden;
        private final TextView tvNombrePunto;
        private final TextView tvCodigoQr;
        private final TextView tvEstadoPunto;
        private final TextView tvDireccion;
        private final TextView tvDistancia;
        private final MaterialButton btnAtender;
        private final MaterialButton btnComoLlegar;
        private final MaterialButton btnIncidencia;

        public PuntoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrden = itemView.findViewById(R.id.tvOrden);
            tvNombrePunto = itemView.findViewById(R.id.tvNombrePunto);
            tvCodigoQr = itemView.findViewById(R.id.tvCodigoQr);
            tvEstadoPunto = itemView.findViewById(R.id.tvEstadoPunto);
            tvDireccion = itemView.findViewById(R.id.tvDireccion);
            tvDistancia = itemView.findViewById(R.id.tvDistancia);
            btnAtender = itemView.findViewById(R.id.btnAtender);
            btnComoLlegar = itemView.findViewById(R.id.btnComoLlegar);
            btnIncidencia = itemView.findViewById(R.id.btnIncidencia);
        }

        public void bind(PuntoRutaEntity punto, Location ubicacionActual, OnPuntoActionListener listener) {
            Context context = itemView.getContext();
            tvOrden.setText(String.valueOf(punto.getOrden()));
            tvNombrePunto.setText(punto.getNombre());
            tvCodigoQr.setText("QR: " + punto.getCodigoQr());
            tvDireccion.setText("Dirección: " + punto.getDireccion());

            String estado = punto.getEstado() != null ? punto.getEstado().toUpperCase() : "PENDIENTE";
            tvEstadoPunto.setText(estado);

            int colorEstado;
            if ("ATENDIDO".equals(estado)) {
                colorEstado = R.color.status_atendido;
                btnAtender.setEnabled(false);
                btnAtender.setText("Atendido");
            } else if ("CON_INCIDENCIA".equals(estado)) {
                colorEstado = R.color.status_observada;
                btnAtender.setEnabled(true);
                btnAtender.setText("Atender");
            } else {
                colorEstado = R.color.status_pendiente;
                btnAtender.setEnabled(true);
                btnAtender.setText("Atender");
            }
            tvEstadoPunto.setBackgroundColor(ContextCompat.getColor(context, colorEstado));

            // Reto R2: Cálculo de Distancia Geodésica y Proximidad
            if (ubicacionActual != null && punto.getLatitud() != 0.0) {
                float distMetros = LocationHelper.calcularDistanciaMetros(
                        ubicacionActual.getLatitude(),
                        ubicacionActual.getLongitude(),
                        punto.getLatitud(),
                        punto.getLongitud()
                );
                if (distMetros < 1000) {
                    tvDistancia.setText(String.format(Locale.getDefault(), "Distancia: %.0f metros", distMetros));
                } else {
                    tvDistancia.setText(String.format(Locale.getDefault(), "Distancia: %.2f km", distMetros / 1000f));
                }
            } else {
                tvDistancia.setText("Distancia: Esperando señal GPS...");
            }

            // Botón Atender
            btnAtender.setOnClickListener(v -> {
                if (listener != null) listener.onAtenderClick(punto);
            });

            // Botón Incidencia
            btnIncidencia.setOnClickListener(v -> {
                if (listener != null) listener.onIncidenciaClick(punto);
            });

            // Botón Cómo Llegar (Google Maps Navigation)
            btnComoLlegar.setOnClickListener(v -> {
                try {
                    String uri = String.format(Locale.US, "google.navigation:q=%f,%f", punto.getLatitud(), punto.getLongitud());
                    Intent navIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    navIntent.setPackage("com.google.android.apps.maps");
                    if (navIntent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(navIntent);
                    } else {
                        // Fallback browser / maps general
                        String geoUri = String.format(Locale.US, "geo:%f,%f?q=%f,%f(%s)",
                                punto.getLatitud(), punto.getLongitud(),
                                punto.getLatitud(), punto.getLongitud(),
                                Uri.encode(punto.getNombre()));
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri)));
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "No se pudo abrir mapas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
