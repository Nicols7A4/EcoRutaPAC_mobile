package pe.edu.usat.ecorutapac.ui.rutas;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import pe.edu.usat.ecorutapac.R;
import pe.edu.usat.ecorutapac.data.local.entity.RutaEntity;
import pe.edu.usat.ecorutapac.databinding.FragmentRutasBinding;
import pe.edu.usat.ecorutapac.ui.common.UiState;
import pe.edu.usat.ecorutapac.ui.rutas.adapter.RutaAdapter;

public class RutasFragment extends Fragment {

    private FragmentRutasBinding binding;
    private RutasViewModel viewModel;
    private RutaAdapter adapter;
    private String estadoFiltroActual = "TODAS";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRutasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(RutasViewModel.class);

        setupRecyclerView();
        setupFilters();
        setupObservers();

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.cargarRutas(null, estadoFiltroActual));
        binding.btnReintentar.setOnClickListener(v -> viewModel.cargarRutas(null, estadoFiltroActual));

        viewModel.cargarRutas(null, estadoFiltroActual);
    }

    private void setupRecyclerView() {
        adapter = new RutaAdapter(ruta -> {
            Intent intent = new Intent(requireContext(), DetalleRutaActivity.class);
            intent.putExtra("id_ruta", ruta.getId());
            startActivity(intent);
        });

        binding.rvRutas.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRutas.setAdapter(adapter);
    }

    private void setupFilters() {
        binding.chipTodos.setOnClickListener(v -> aplicarFiltro("TODAS"));
        binding.chipProgramadas.setOnClickListener(v -> aplicarFiltro("PROGRAMADA"));
        binding.chipEnCurso.setOnClickListener(v -> aplicarFiltro("EN_CURSO"));
        binding.chipFinalizadas.setOnClickListener(v -> aplicarFiltro("FINALIZADA"));
        binding.chipObservadas.setOnClickListener(v -> aplicarFiltro("OBSERVADA"));
    }

    private void aplicarFiltro(String estado) {
        this.estadoFiltroActual = estado;
        viewModel.cargarRutas(null, estadoFiltroActual);
    }

    private void setupObservers() {
        viewModel.getRutasState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            binding.swipeRefresh.setRefreshing(false);

            if (state.getEstado() == UiState.Estado.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.llEmpty.setVisibility(View.GONE);
                binding.llError.setVisibility(View.GONE);
            } else if (state.getEstado() == UiState.Estado.CONTENT) {
                binding.progressBar.setVisibility(View.GONE);
                binding.llEmpty.setVisibility(View.GONE);
                binding.llError.setVisibility(View.GONE);
                binding.rvRutas.setVisibility(View.VISIBLE);
                adapter.setRutas(state.getData());
            } else if (state.getEstado() == UiState.Estado.EMPTY) {
                binding.progressBar.setVisibility(View.GONE);
                binding.rvRutas.setVisibility(View.GONE);
                binding.llError.setVisibility(View.GONE);
                binding.llEmpty.setVisibility(View.VISIBLE);
            } else if (state.getEstado() == UiState.Estado.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                binding.rvRutas.setVisibility(View.GONE);
                binding.llEmpty.setVisibility(View.GONE);
                binding.llError.setVisibility(View.VISIBLE);
                binding.tvErrorMessage.setText(state.getMensaje());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.cargarRutas(null, estadoFiltroActual);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
