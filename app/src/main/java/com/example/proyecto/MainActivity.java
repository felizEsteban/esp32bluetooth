package com.example.proyecto;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyecto.databinding.ActivityMainBinding;
import com.example.proyecto.ui.bondeddevices.BondedDevicesFragment;
import com.example.proyecto.ui.datarcv.DataRcvFragment;
import com.example.proyecto.ui.datatxd.DataTxdFragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1001;

    private ActivityMainBinding binding;
    private SharedViewModel sharedViewModel;
    private BluetoothAdapter bluetoothAdapter;
    private boolean pausedDueToBluetooth = false;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private ConnectedThread connectedThread = null;
    private Handler bluetoothIn;
    final int handlerState = 0;
    private StringBuilder dataStringIn = new StringBuilder();
    private final BroadcastReceiver bluetoothReceiver;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "BluetoothApp";

    public MainActivity() {
        bluetoothReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state == BluetoothAdapter.STATE_OFF) {
                    pausedDueToBluetooth = true;
                    Toast.makeText(MainActivity.this, "Bluetooth desactivado", Toast.LENGTH_SHORT).show();
                } else if (state == BluetoothAdapter.STATE_ON && pausedDueToBluetooth) {
                    pausedDueToBluetooth = false;
                    Toast.makeText(MainActivity.this, "Bluetooth activado", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
                return;
            }
        }

        iniciarTodo(); // Solo si ya se tienen permisos
    }

    private void iniciarTodo() {
        initializeUI();
        setupBluetooth();
        setupMessageHandler();
        setupObservers();
    }

    private void initializeUI() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new BondedDevicesFragment())
                .commit();

        binding.btnBonded.setText("Dispositivos");
        binding.btnData.setText("Datos");
        binding.btnTx.setText("Control");

        binding.btnBonded.setOnClickListener(v -> showFragment(new BondedDevicesFragment()));
        binding.btnData.setOnClickListener(v -> showFragment(new DataRcvFragment()));
        binding.btnTx.setOnClickListener(v -> showFragment(new DataTxdFragment()));
    }

    private void setupBluetooth() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth no disponible", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothReceiver, filter);

        if (!bluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
        }
    }

    private void requestBluetoothEnable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "No tienes permiso para usar Bluetooth", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }


    private void setupMessageHandler() {
        bluetoothIn = new Handler(msg -> {
            if (msg.what == handlerState) {
                String receivedData = (String) msg.obj;
                dataStringIn.append(receivedData);

                int endOfLineIndex = dataStringIn.indexOf("#");
                if (endOfLineIndex > 0) {
                    String completeMessage = dataStringIn.substring(0, endOfLineIndex);
                    dataStringIn.delete(0, dataStringIn.length() + 1);

                    if (completeMessage.contains("Humidity") && completeMessage.contains("Temperature")) {
                        String[] parts = completeMessage.split("\\$");
                        if (parts.length >= 5) {
                            String humidity = parts[2];
                            String temperature = parts[4];
                            sharedViewModel.setDataIn("Humedad: " + humidity + "%\nTemp: " + temperature + "°C");
                        }
                    } else if (completeMessage.contains(",")) {
                        String[] values = completeMessage.split(",");
                        if (values.length == 2) {
                            sharedViewModel.setDataIn("Humedad: " + values[0] + "%\nTemp: " + values[1] + "°C");
                        }
                    }
                }
            }
            return true;
        });
    }

    private void setupObservers() {
        sharedViewModel.getDeviceAddress().observe(this, address -> {
            if (!address.equals("no address")) {
                connectToDevice(address);
            }
        });

        sharedViewModel.getDataOut().observe(this, data -> {
            if (connectedThread == null) {
                Toast.makeText(this, "No hay conexión activa con el dispositivo", Toast.LENGTH_SHORT).show();
                return;
            }
            if (data != null && !data.isEmpty()) {
                connectedThread.write(data.endsWith("#") ? data : data + "#");
            }

        });
    }

    private void connectToDevice(String address) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso Bluetooth no concedido", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);

                try {
                    Method m = bluetoothDevice.getClass().getMethod("setPin", byte[].class);
                    m.invoke(bluetoothDevice, "1234".getBytes());
                } catch (Exception e) {
                    Log.e(TAG, "Error al configurar PIN", e);
                }

                if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Ya estás conectado a este dispositivo", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(BTMODULEUUID);
                bluetoothSocket.connect();

                runOnUiThread(() -> {
                    connectedThread = new ConnectedThread(bluetoothSocket);
                    connectedThread.start();
                    Toast.makeText(MainActivity.this, "Conectado a " + bluetoothDevice.getName(), Toast.LENGTH_SHORT).show();
                });

            } catch (IOException e) {
                Log.e(TAG, "Error en conexión Bluetooth", e);
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "No se pudo conectar al dispositivo", Toast.LENGTH_SHORT).show()
                );
                try {
                    if (bluetoothSocket != null) bluetoothSocket.close();
                } catch (IOException ex) {
                    Log.e(TAG, "Error al cerrar socket", ex);
                }
            }
        }).start();
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private volatile boolean isRunning = true;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error al crear streams", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (isRunning) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "Error en lectura Bluetooth", e);
                    break;
                }
            }
        }

        public void write(String input) {
            try {
                mmOutStream.write(input.getBytes());
            } catch (IOException e) {
                Log.e(TAG, "Error al escribir en Bluetooth", e);
                Toast.makeText(MainActivity.this, "Error al enviar", Toast.LENGTH_SHORT).show();
            }
        }

        public void cancel() {
            isRunning = false;
            try {
                mmInStream.close();
                mmOutStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error al cerrar streams", e);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth activado 😎", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth no activado 💥", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarTodo(); // ← ya podemos mostrar la UI
            } else {
                Toast.makeText(this, "Permiso Bluetooth denegado ❌", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectedThread != null) {
            connectedThread.cancel();
        }
        try {
            unregisterReceiver(bluetoothReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Receiver no estaba registrado", e);
        }
    }
}
