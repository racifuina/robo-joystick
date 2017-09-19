package com.acifuina.robojoystick;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by racifuina on 11/09/17.
 */

public class BLEConnectionManager extends BluetoothGattCallback {

    final public static UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public static BleManagerCallback bleManagerCallback;
    private static BLEConnectionManager _ds = null;
    private static BluetoothAdapter mBluetoothAdapter;
    public static Boolean isConnected = false;
    private static BluetoothDevice mBluetoothDevice;
    private static BluetoothGatt mBluetoothGatt;
    private static BluetoothGattCharacteristic mBluetoothGattCharacteristic;
    static BluetoothSocket bluetoothSocket;

    public static BLEConnectionManager getInstance() {
        if (_ds == null) {
            _ds = new BLEConnectionManager();
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return _ds;
    }

    public BluetoothAdapter bluetoothAdapter() {
        return mBluetoothAdapter;
    }

    void connectTo (BluetoothDevice device) {
        mBluetoothAdapter.cancelDiscovery();
        try {
            BluetoothSocket socketConnection = device.createRfcommSocketToServiceRecord(myUUID);
            socketConnection.connect();
            mBluetoothDevice = device;
            isConnected = true;
            bluetoothSocket = socketConnection;
            if (bleManagerCallback != null) bleManagerCallback.onBLEDeviceConnected();
        } catch (IOException e) {
            isConnected = false;
            if (bleManagerCallback != null)  bleManagerCallback.onBLEDeviceDisconnected();
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            isConnected = false;
            if (bleManagerCallback != null)  bleManagerCallback.onBLEDeviceDisconnected();
        }

//        disconnectModule();
//        device.connectGatt(RoboJoystick.getInstance(), true, this);
    }

    interface BleManagerCallback {
        void onBLEDeviceConnected();
        void onBLEDeviceDisconnected();
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            System.out.println("STATE_CONNECTED");
            if (mBluetoothGatt == null) {
                gatt.discoverServices();
            } else {
                isConnected = true;
               if (bleManagerCallback != null) bleManagerCallback.onBLEDeviceConnected();
            }
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            System.out.println("STATE_DISCONNECTED");
            isConnected = false;
            if (bleManagerCallback != null)  bleManagerCallback.onBLEDeviceDisconnected();
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        System.out.println("onServicesDiscovered  ++++++++++++   BUSCANDO SERVICIOS  ++++++++++++");
        for (BluetoothGattService gattService : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
                if (characteristic.getUuid().toString().equals("0000ffe1-0000-1000-8000-00805f9b34fb")) {
                    mBluetoothGattCharacteristic = characteristic;
                    mBluetoothGatt = gatt;
                    mBluetoothGatt.setCharacteristicNotification(mBluetoothGattCharacteristic, true);
                    mBluetoothDevice = gatt.getDevice();
                    isConnected = true;
                    if (bleManagerCallback != null) bleManagerCallback.onBLEDeviceConnected();
                }
            }
        }
    }

    void disconnectModule() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.setCharacteristicNotification(mBluetoothGattCharacteristic, false);
            mBluetoothGatt.close();
            mBluetoothGatt.disconnect();
            mBluetoothDevice = null;
            mBluetoothGatt = null;
            mBluetoothGattCharacteristic = null;
        }
    }
    void disconnect() {
        if (isConnected && bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    void enviarComando(String comando) {
        if (isConnected) {
            if (mBluetoothGatt != null) {
                mBluetoothGattCharacteristic.setValue(comando);
                mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic);
            }
        }

        if (isConnected && bluetoothSocket != null) {
            try {
                bluetoothSocket.getOutputStream().write(comando.getBytes());
                System.out.println("writing! comand!!! +++++++++");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
