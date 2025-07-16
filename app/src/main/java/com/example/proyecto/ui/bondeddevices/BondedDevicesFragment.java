package com.example.proyecto.ui.bondeddevices;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto.R;
import com.example.proyecto.SharedViewModel;
import com.example.proyecto.databinding.FragmentBondedDevicesBinding;

import java.util.Set;

public class BondedDevicesFragment extends Fragment {
    private BluetoothAdapter bluetoothAdapter;
    private FragmentBondedDevicesBinding binding;
    private String address=null;
    private SharedViewModel sharedViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentBondedDevicesBinding.inflate(inflater,container,false);
        sharedViewModel=new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        return binding.getRoot();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayAdapter<String> pairedDevicesArrayadapter=new ArrayAdapter<>(getContext(), R.layout.found_devices);
        binding.lvBonded.setAdapter(pairedDevicesArrayadapter);
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices=bluetoothAdapter.getBondedDevices();
        if(pairedDevices.size()>0){
            for(BluetoothDevice device: pairedDevices){
                pairedDevicesArrayadapter.add(device.getName()+"\n"+device.getAddress());
            }
        }
        binding.lvBonded.setOnItemClickListener((av,v,arg2,arg3)->{
            String info=((TextView) v).getText().toString();
            address=info.substring(info.length()-17);
            sharedViewModel.setDeviceAddress(address);
            Toast.makeText(getContext(), "address-> "+address, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding=null;
    }
}