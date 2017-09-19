package com.acifuina.robojoystick;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class FindDevicesActivity extends AppCompatActivity implements BLEConnectionManager.BleManagerCallback{
    private ProgressDialog progressDialog;
    private ListView listView;

    private final static int REQUEST_ENABLE_BT = 1;
    private BroadcastReceiver broadcastReceiver;

    private ArrayList<BluetoothDevice> dispositivosBluetooth;
    private DispositivosBTAdapter dispositivosBTAdapter;

    private BLEConnectionManager bleConnectionManager = BLEConnectionManager.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_devices);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

//        iniciar();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanDevices();
            }
        });

        progressDialog = new ProgressDialog(FindDevicesActivity.this);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                bleConnectionManager.bluetoothAdapter().cancelDiscovery();
            }
        });

        bleConnectionManager.bleManagerCallback = this;

        dispositivosBluetooth = new ArrayList<BluetoothDevice>();
        listView = (ListView)findViewById(R.id.listView);
        dispositivosBTAdapter = new DispositivosBTAdapter();
        listView.setAdapter(dispositivosBTAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                progressDialog.setTitle("Conectando con el dispositivo " + dispositivosBluetooth.get(i).getName());
                progressDialog.setMessage("Por favor espere...");
                progressDialog.show();
                bleConnectionManager.connectTo(dispositivosBluetooth.get(i));
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    progressDialog.setTitle("Buscando dispositivos Bluetooth");
                    progressDialog.setMessage("Por favor espere...");
                    progressDialog.show();
                    dispositivosBluetooth.clear();
                    dispositivosBTAdapter.notifyDataSetChanged();
                }
                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    progressDialog.dismiss();
                }
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    dispositivosBluetooth.add(device);
                    dispositivosBTAdapter.notifyDataSetChanged();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, intentFilter);
        scanDevices();
    }
    private void scanDevices() {
        if (!bleConnectionManager.bluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            bleConnectionManager.bluetoothAdapter().startDiscovery();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
            bleConnectionManager.bluetoothAdapter().startDiscovery();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (bleConnectionManager.bluetoothAdapter().isDiscovering()) {
            unregisterReceiver(broadcastReceiver);
            bleConnectionManager.bluetoothAdapter().cancelDiscovery();
        }
    }

    private class DispositivosBTAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return dispositivosBluetooth.size();
        }

        @Override
        public Object getItem(int position) {
            return dispositivosBluetooth.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BluetoothDevice currentDevice = dispositivosBluetooth.get(position);
            convertView = getLayoutInflater().inflate(R.layout.bt_device_cell, null);
            TextView nombreDispositivoTextView = (TextView) convertView.findViewById(R.id.nombreDispositivoTextView);
            TextView direccionDispositivoTextView = (TextView) convertView.findViewById(R.id.direccionTextView);
            nombreDispositivoTextView.setText(currentDevice.getName());
            direccionDispositivoTextView.setText(currentDevice.getAddress());
            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_find_devices, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_credits) {
            Intent intent = new Intent(this, CreditosActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBLEDeviceConnected() {
        System.out.println("onBLEDeviceConnected ActivityFindDevices");
        FindDevicesActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                unregisterReceiver(broadcastReceiver);
                progressDialog.dismiss();
                iniciar();
            }
        });
    }

    @Override
    public void onBLEDeviceDisconnected() {
        System.out.println("onBLEDeviceDisconnected");
    }

    private void iniciar() {
        Intent goToNueva = new Intent(this, JoysticksActivity.class);
        startActivity(goToNueva);
        finish();
    }

}
