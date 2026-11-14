package pe.edu.usat.ecorutapac.ui.rutas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pe.edu.usat.ecorutapac.R;
import pe.edu.usat.ecorutapac.data.local.entity.RutaEntity;

public class RutaAdapter extends RecyclerView.Adapter<RutaAdapter.RutaViewHolder> {

    public interface OnRutaClickListener {
        void onRutaClick(RutaEntity ruta);
    }

    private List<RutaEntity> listaRutas = new ArrayList<>();
    private final OnRutaClickListener listener;

    public RutaAdapter(OnRutaClickListener listener) {
        this.listener = listener;
    }

    public void setRutas(List<RutaEntity> rutas) {
        this.listaRutas = rutas != null ? rutas : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RutaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ruta, parent, false);
        return new RutaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutaViewHolder holder, int position) {
        RutaEntity ruta = listaRutas.get(position);
        holder.bind(ruta, listener);
    }

    @Override
    public int getItemCount() {
        return listaRutas.size();
    }

    static class RutaViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCodigo;
        private final TextView tvEstadoBadge;
        private final TextView tvFechaTurno;
        private final TextView tvOperario;
        private final TextView tvVehiculo;
        private final ProgressBar pbAvance;
        private final TextView tvPuntosAvance;

        public RutaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCodigo = itemView.findViewById(R.id.tvCodigo);
            tvEstadoBadge = itemView.findViewById(R.id.tvEstadoBadge);
            tvFechaTurno = itemView.findViewById(R.id.tvFechaTurno);
            tvOperario = itemView.findViewById(R.id.tvOperario);
            tvVehiculo = itemView.findViewById(R.id.tvVehiculo);
            pbAvance = itemView.findViewById(R.id.pbAvance);
            tvPuntosAvance = itemView.findViewById(R.id.tvPuntosAvance);
        }

        public void bind(RutaEntity ruta, OnRutaClickListener listener) {
            tvCodigo.setText(ruta.getCodigo());
            tvEstadoBadge.setText(ruta.getEstado());

            int colorRes;
            switch (ruta.getEstado() != null ? ruta.getEstado().toUpperCase() : "") {
                case "EN CURSO":
                    colorRes = R.color.status_en_curso;
                    break;
                case "FINALIZADA":
                    colorRes = R.color.status_finalizada;
                    break;
                case "OBSERVADA":
                    colorRes = R.color.status_observada;
                    break;
                case "PROGRAMADA":
                default:
                    colorRes = R.color.status_programada;
                    break;
            }
            tvEstadoBadge.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), colorRes));

            tvFechaTurno.setText("Fecha: " + ruta.getFecha() + " | Turno: " + (ruta.getTurno() != null ? ruta.getTurno() : "General"));
            tvOperario.setText("Operario: " + (ruta.getNombreOperario() != null ? ruta.getNombreOperario() : "Sin asignar"));
            tvVehiculo.setText("Vehículo: " + (ruta.getPlacaVehiculo() != null ? ruta.getPlacaVehiculo() : "Sin asignar"));

            pbAvance.setProgress(ruta.getPorcentajeAvance());
            tvPuntosAvance.setText(ruta.getPuntosAtendidos() + "/" + ruta.getTotalPuntos() + " (" + ruta.getPorcentajeAvance() + "%)");

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRutaClick(ruta);
                }
            });
        }
    }
}
