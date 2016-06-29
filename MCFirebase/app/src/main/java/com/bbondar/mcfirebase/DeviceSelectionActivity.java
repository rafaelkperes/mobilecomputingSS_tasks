package com.bbondar.mcfirebase;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by bbondar on 21-May-16.
 */
public class DeviceSelectionActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {
    public final static String TAG = DeviceSelectionActivity.class.getName();

    public static final String RESULT_BLUETOOTHDEVICE = "BluetoothDevice";

    public static final int REQUEST_ENABLE_BT = 1;

    // Scan timeout in milliseconds.
    public static final int SCAN_TIMEOUT = 15000;

    public static final int SELECTED_NONE = -1;
    private BluetoothAdapter bluetoothAdapter = null;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private List<BluetoothDevice> devices;
    private List<String> devicesList;

    private Handler timeoutHandler;

    private boolean isScanning = false;

    private ListView deviceListView;
    private ArrayAdapter<String> deviceListAdapter;

    private ItemClickListener itemClickListener;
    private TextView textviewScanstatus;

    private int selected;

    private class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selected = position;
            finished();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_selection);

        if (bluetoothAdapter == null) {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(
                    Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        selected = SELECTED_NONE;
        devices = new LinkedList<>();
        devicesList = new LinkedList<>();
        deviceListAdapter = new ArrayAdapter<>(this, R.layout.textview_deviceselection,
                devicesList);
        deviceListView = (ListView) findViewById(R.id.listView_deviceselection);
        deviceListView.setAdapter(deviceListAdapter);
        deviceListView.setOnItemClickListener(new ItemClickListener());

        Button startScan = (Button) findViewById(R.id.scanButton);
        startScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanning();
            }
        });

        textviewScanstatus = (TextView) findViewById(R.id.textViewScanStatus);
    }

    @Override
    public void onStart() {
        super.onStart();

        startScanning();
    }

    @Override
    public void onStop() {
        super.onStop();

        stopScanning();
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(TAG, "Discovered device: " + device.getName() + " " + device.getAddress());

        // Check, whether we have already seen this device
        for (BluetoothDevice d : devices) {
            if (d.hashCode() == device.hashCode()) {
                // This device, we know already
                return;
            }
        }

        // Device is new

        String newDeviceString = new String(device.getName() + " [" + device.getAddress() + "]");
        devices.add(device);
        devicesList.add(newDeviceString);
        //deviceListAdapter.notifyDataSetChanged();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceListAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Stop Bluetooth scan.
     */
    private void stopScanning() {
        Log.i(TAG, "scanning stopped");

        if (!isScanning){
            return;
        }

        textviewScanstatus.setText("Not scanning");

        bluetoothAdapter.stopLeScan(this);
        isScanning = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission granted: coarse location");
                    startScanning();
                } else {
                    finished();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT :
                if (resultCode == RESULT_OK) {
                    // BLE has been enabled
                    startScanning();
                } else {
                    // Cannot scan for devices w/o Bluetooth -> return (without result)
                    finished();
                }
                break;
        }
    }

    /**
     * Start Bluetooth scan.
     */
    private void startScanning() {
        Log.i(TAG, "scanning started");

        // In Android M, background Bluetooth scanning requires permission to coarse locations.
        // With the new permission system of Android M, we need to request permissions
        // at runtime.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Permission dialog");
                builder.setMessage("Location permission");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    PERMISSION_REQUEST_COARSE_LOCATION);
                        }
                    }
                });
                builder.show();
                return; // We come back later, when user has granted access.
            }
        }

        if (isScanning) {
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
            return;
        }

        textviewScanstatus.setText("Scanning...");

        devices.clear();
        devicesList.clear();
        deviceListAdapter.notifyDataSetChanged();

        isScanning = true;
        bluetoothAdapter.startLeScan(this);

        // Stop scanning after timeout.
        // The handler is running on the UI thread.
        timeoutHandler = new Handler(Looper.getMainLooper());
        timeoutHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DeviceSelectionActivity.this.stopScanning();
            }
        }, SCAN_TIMEOUT);
    }

    /**
     * Finish the activity, with or without result.
     */
    private void finished() {
        stopScanning();

        if (selected == SELECTED_NONE) {
            // Nothing selected -> finish without result.
            Intent result = new Intent();
            setResult(Activity.RESULT_CANCELED, result);
        } else {
            // Device selected -> finish with result
            BluetoothDevice selectedDevice = devices.get(selected);
            Intent result = new Intent();
            result.putExtra(RESULT_BLUETOOTHDEVICE, selectedDevice);
            setResult(Activity.RESULT_OK, result);
        }

        finish();
    }
}
