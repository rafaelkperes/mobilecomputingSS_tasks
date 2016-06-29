package com.bbondar.mcfirebase;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getName();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_SELECT_DEVICE = 2;

    // Standard base UUID (used to extend standard 16 bit UUIDs)
    public static final long STANDARD_BASE_UUID_MSB = 0x0000000000001000L;
    public static final long STANDARD_BASE_UUID_LSB = 0x800000805f9b34fbL;
    // 16 bit id of client characteristic configuration descriptor
    public static final short CLIENT_CHARACTERISTIC_CONFIGURATION_ID = 0x2902;

    private static final String WEATHER_SERVICE_UUID =  "00000002-0000-0000-FDFD-FDFDFDFDFDFD";
    private static final short tempCharUUIDShort = 0x2A1C;
    private static final UUID tempCharUUID = getUUID(STANDARD_BASE_UUID_MSB,STANDARD_BASE_UUID_LSB,tempCharUUIDShort);
    private static final UUID clientCharUUID = getUUID(STANDARD_BASE_UUID_MSB,STANDARD_BASE_UUID_LSB,CLIENT_CHARACTERISTIC_CONFIGURATION_ID);

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice bluetoothDevice;

    private GattCallbackHandler gattHandler;
    private BluetoothGatt gatt;
    private BluetoothGattService weatherService = null;
    private BluetoothGattCharacteristic temperatureCharacteristic = null;

    private FirebaseDatabase database;
    private DatabaseReference root;
    private Button readTempButton;
    private SimpleDateFormat ymd =
            new SimpleDateFormat ("yyyy-MM-dd");
    private SimpleDateFormat ymdhms =
            new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Button selectDevice = (Button) findViewById(R.id.bleScanButton);
        selectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDevice();
            }
        });
        gattHandler = new GattCallbackHandler();

        database = FirebaseDatabase.getInstance();
        root = database.getReference("team16");
        root.child("uuid").child(WEATHER_SERVICE_UUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Firebase", "Data is: " + dataSnapshot);
                HashMap<String, String> values = (HashMap<String, String>) dataSnapshot.getValue();
                String lastTime = "0";
                for(String timeStamp : values.keySet()){
                    if(Long.parseLong(timeStamp) > Long.parseLong(lastTime))
                        lastTime = timeStamp;
                }
                Date time = new Date(Long.parseLong(lastTime));
                TextView tempLabel = (TextView) findViewById(R.id.tempLabel);
                tempLabel.setText(ymdhms.format(time) + " - " + values.get(lastTime) + " C");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("Firebase", "Failed to read value.", error.toException());
            }
        });
        final Date dNow = new Date( );
        root.child("location").child("Stuttgart").child(ymd.format(dNow)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Firebase", "Data is: " + dataSnapshot);
                HashMap<String, String> values = (HashMap<String, String>) dataSnapshot.getValue();
                double tempSum = 0;
                for(String temp : values.values()){
                    tempSum += Double.parseDouble(temp);
                }
                NumberFormat formatter = new DecimalFormat("#0.00");
                TextView avgTempLabel = (TextView) findViewById(R.id.avgTempLabel);
                avgTempLabel.setText(ymd.format(dNow) + " - " + formatter.format(tempSum/values.size()*1.0) + " C");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Firebase", "Failed to read value.", databaseError.toException());
            }
        });

        readTempButton = (Button) findViewById(R.id.readTempButton);
        readTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gatt.readCharacteristic(temperatureCharacteristic);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        finishTask();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    // Bluetooth has been enabled. Select device.
                    selectDevice();
                } else {
                    // No Bluetooth available. Cancel task.
                    finishTask();
                }
                break;
            case REQUEST_SELECT_DEVICE:
                if (resultCode == RESULT_OK) {
                    // Bluetooth device has been selected. Continue task.
                    bluetoothDevice = data.getParcelableExtra(
                            DeviceSelectionActivity.RESULT_BLUETOOTHDEVICE);
                    TextView selectedDeviceName = (TextView) findViewById(R.id.selectedDeviceNameView);
                    selectedDeviceName.setText(bluetoothDevice.getName());
                    bluetoothDevice.connectGatt(this, false, gattHandler);
                } else {
                    // No suitable Bluetooth device found. Cancel task.
                    finishTask();
                }
                break;
        }
    }

    /**
     * Trigger selection of Bluetooth device in dedicated activity.
     */
    private void selectDevice() {
        Intent intent = new Intent(this, DeviceSelectionActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_DEVICE);
    }

    /**
     * Handler for GATT callbacks.
     */
    private class GattCallbackHandler extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Bluetooth: connected");
                MainActivity.this.gatt = gatt;
                Log.i(TAG, "connected to " + gatt.getDevice().getName());
                Log.i(TAG, "Attempting to start service discovery:" +
                        gatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                finishTask();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                // Service discovery failed. Cancel task.
                Log.i(TAG, "Service discovery failed");
                finishTask();
            } else {
                // Service discovered. Continue current task.
                Log.i(TAG, "Service discovered" + gatt.getServices());
                weatherService = gatt.getService(UUID.fromString(WEATHER_SERVICE_UUID));
                temperatureCharacteristic = weatherService.getCharacteristic(tempCharUUID);
                setCharacteristicNotification(temperatureCharacteristic, true);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "Read value: " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1)*0.01);
            int temperature = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1);
            String value = String.valueOf(temperature * 0.01);
            String tempValue = value.substring(0, value.length() > 5 ? 5 : value.length());
            root.child("uuid").child(WEATHER_SERVICE_UUID).child(String.valueOf(System.currentTimeMillis())).setValue(tempValue);
            Date dNow = new Date( );
            root.child("location").child("Stuttgart").child(ymd.format(dNow)).push().setValue(tempValue);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "Temp value: " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1)*0.01);
        }
    }

    /**
     * Finish a running Bluetooth task and release GATT resources.
     */
    synchronized private void finishTask() {
        Log.i(TAG, "Finishing Bluetooth task");

        // Release all GATT resources
        if (gatt != null) {
            gatt.close();
            gatt = null;
            Log.i(TAG, "Bluetooth: diconnecting");
        }

    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        gatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(clientCharUUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    /**
     * Creates a 128 bit UUID of a service or characteristic from a 128 base UUID and 16 bit
     * service/characteristic id.
     *
     * Example: Given
     * - 128 bit base UUID 550eXXXX-e29b-11d4-a716-446655440000
     * - 16 bit service ID: 0x1234
     * The resulting UUID is generated by replacing XXXX by the 16 bit id of the service:
     * - UUID: 550e1234-e29b-11d4-a716-446655440000
     *
     * @param baseMSB most significant bits of the base UUID
     * @param baseLSB least significant bits of the base UUID
     * @param id 16 bit id of the service or characteristic
     * @return UUID of the service of characteristic
     */
    public static UUID getUUID(long baseMSB, long baseLSB, short id) {
        long msb = baseMSB & 0xffff0000ffffffffL;
        msb |= ((long) id)<<32;

        return new UUID(msb, baseLSB);
    }
}
