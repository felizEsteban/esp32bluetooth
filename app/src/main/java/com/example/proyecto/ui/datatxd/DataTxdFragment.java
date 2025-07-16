package com.example.proyecto.ui.datatxd;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyecto.R;
import com.example.proyecto.SharedViewModel;
import com.example.proyecto.databinding.FragmentDataTxdBinding;

public class DataTxdFragment extends Fragment {
    private FragmentDataTxdBinding binding;
    private SharedViewModel sharedViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDataTxdBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        setupLedControls();
        return binding.getRoot();
    }

    private void setupLedControls() {
        // Botón para encender LED (envía "1#")
        binding.btnLedOn.setOnClickListener(v -> {
            sharedViewModel.setDataOut("1#");
            showLedStatus(true);
        });

        // Botón para apagar LED (envía "5#")
        binding.btnLedOff.setOnClickListener(v -> {
            sharedViewModel.setDataOut("5#");
            showLedStatus(false);
        });
    }

    private void showLedStatus(boolean isOn) {
        String message = isOn ? "LED encendido" : "LED apagado";
        int color = isOn ? R.color.purple_200 : R.color.teal_700;

        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        binding.ledStatus.setText(message);
        binding.ledStatus.setTextColor(getResources().getColor(color));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}