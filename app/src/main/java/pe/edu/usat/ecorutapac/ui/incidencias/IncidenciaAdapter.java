package pe.edu.usat.ecorutapac.ui.incidencias;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pe.edu.usat.ecorutapac.R;
import pe.edu.usat.ecorutapac.data.local.entity.IncidenciaEntity;

public class IncidenciaAdapter extends RecyclerView.Adapter<IncidenciaAdapter.IncidenciaViewHolder> {

    public interface OnCambiarEstadoListener {
        void onCambiarEstado(IncidenciaEntity incidencia);
    }

    private List<IncidenciaEntity> lista = new ArrayList<>();
    private final boolean esSupervisorOFiscal;
    private final OnCambiarEstadoListener listener;

    public IncidenciaAdapter(boolean esSupervisorOFiscal, OnCambiarEstadoListener listener) {
        this.esSupervisorOFiscal = esSupervisorOFiscal;
        this.listener = listener;
    }

    public void setIncidencias(List<IncidenciaEntity> incidencias) {
        this.lista = incidencias != null ? incidencias : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IncidenciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incidencia, parent, false);
        return new IncidenciaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidenciaViewHolder holder, int position) {
        IncidenciaEntity item = lista.get(position);
        holder.bind(item, esSupervisorOFiscal, listener);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class IncidenciaViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTipo;
        private final TextView tvEstadoIncidencia;
        private final TextView tvPunto;
        private final TextView tvDescripcion;
        private final ImageView ivFotoMiniatura;
        private final TextView tvAutorFecha;
        private final MaterialButton btnCambiarEstadoIncidencia;

        public IncidenciaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvEstadoIncidencia = itemView.findViewById(R.id.tvEstadoIncidencia);
            tvPunto = itemView.findViewById(R.id.tvPunto);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            ivFotoMiniatura = itemView.findViewById(R.id.ivFotoMiniatura);
            tvAutorFecha = itemView.findViewById(R.id.tvAutorFecha);
            btnCambiarEstadoIncidencia = itemView.findViewById(R.id.btnCambiarEstadoIncidencia);
        }

        public void bind(IncidenciaEntity item, boolean esSupervisorOFiscal, OnCambiarEstadoListener listener) {
            Context context = itemView.getContext();
            tvTipo.setText(item.getTipo());
            tvEstadoIncidencia.setText(item.getEstado());
            tvPunto.setText("Punto: " + (item.getNombrePunto() != null ? item.getNombrePunto() : "Punto ID " + item.getIdRutaPunto()));
            tvDescripcion.setText(item.getDescripcion());
            tvAutorFecha.setText("Registrado: " + item.getFechaRegistro() + " | Por: " + (item.getNombreUsuario() != null ? item.getNombreUsuario() : "Operario"));

            // Reto R3: Cargar miniatura fotográfica de evidencia
            String fotoLocal = item.getFotoRutaLocal();
            String fotoRemota = item.getFotoUrlRemota();

            if (!TextUtils.isEmpty(fotoLocal) && new File(fotoLocal).exists()) {
                ivFotoMiniatura.setVisibility(View.VISIBLE);
                Glide.with(context).load(new File(fotoLocal)).into(ivFotoMiniatura);
            } else if (!TextUtils.isEmpty(fotoRemota)) {
                ivFotoMiniatura.setVisibility(View.VISIBLE);
                Glide.with(context).load(fotoRemota).into(ivFotoMiniatura);
            } else {
                ivFotoMiniatura.setVisibility(View.GONE);
            }

            if (esSupervisorOFiscal && !"RESUELTA".equalsIgnoreCase(item.getEstado())) {
                btnCambiarEstadoIncidencia.setVisibility(View.VISIBLE);
                btnCambiarEstadoIncidencia.setOnClickListener(v -> {
                    if (listener != null) listener.onCambiarEstado(item);
                });
            } else {
                btnCambiarEstadoIncidencia.setVisibility(View.GONE);
            }
        }
    }
}
