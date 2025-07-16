package com.example.proyecto.ui.datarcv;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.proyecto.SharedViewModel;
import com.example.proyecto.databinding.FragmentDataRcvBinding;

public class DataRcvFragment extends Fragment {
    private FragmentDataRcvBinding binding;
    private SharedViewModel sharedViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDataRcvBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        sharedViewModel.getDeviceAddress().observe(getViewLifecycleOwner(), address -> {
            if (address != null && !address.isEmpty()) {
                binding.tvAddressValue.setTextSize(34);
                binding.tvAddressValue.setTextColor(Color.WHITE);
                binding.tvAddressValue.setText(address);
            } else {
                binding.tvAddressValue.setTextSize(18);
                binding.tvAddressValue.setTextColor(Color.LTGRAY);
                binding.tvAddressValue.setText("No conectado");
            }
        });

        sharedViewModel.getDataIn().observe(getViewLifecycleOwner(), data -> {
            if (data != null && !data.isEmpty()) {
                binding.tvDataRcvValue.setTextSize(24);
                binding.tvDataRcvValue.setTextColor(Color.WHITE);
                binding.tvDataRcvValue.setText(data);
            } else {
                binding.tvDataRcvValue.setTextSize(18);
                binding.tvDataRcvValue.setTextColor(Color.LTGRAY);
                binding.tvDataRcvValue.setText("Esperando datos...");
            }
        });

        return binding.getRoot();
    }
}
