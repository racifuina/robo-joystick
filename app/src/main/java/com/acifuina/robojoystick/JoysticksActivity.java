package com.acifuina.robojoystick;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class JoysticksActivity extends AppCompatActivity {

    private TextView mTextViewAngleLeft;
    private TextView mTextViewStrengthLeft;
    private RelativeLayout backgroundView;
    private TextView mTextViewAngleRight;
    private TextView mTextViewStrengthRight;
    private List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));
    private JoystickPositions leftJoystickPosition, rightJoystickPosition;
    private Menu menu;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (blockedKeys.contains(event.getKeyCode())) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
                backgroundView.setBackgroundColor(getResources().getColor(R.color.arduino_orange));
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
                backgroundView.setBackgroundColor(getResources().getColor(R.color.white_color));
            }
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }
    //The BroadcastReceiver that listens for bluetooth broadcasts
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                menu.getItem(0).setIcon(getDrawable(R.drawable.ic_bluetooth_connected_white_24dp));
            }

            if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                menu.getItem(0).setIcon(getDrawable(R.drawable.ic_bluetooth_disabled_white_24dp));
            }

            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                menu.getItem(0).setIcon(getDrawable(R.drawable.ic_bluetooth_disabled_white_24dp));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joysticks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTextViewAngleLeft = (TextView) findViewById(R.id.textView_angle_left);
        mTextViewStrengthLeft = (TextView) findViewById(R.id.textView_strength_left);
        backgroundView = (RelativeLayout) findViewById(R.id.backgroundView);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);


        JoystickView joystickLeft = (JoystickView) findViewById(R.id.joystickView_left);
        joystickLeft.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                mTextViewAngleLeft.setText(angle + "°");
                mTextViewStrengthLeft.setText(strength + "%");

                if (strength > 10) {
                    if (angle < 180) {
                        leftJoystickPosition = JoystickPositions.forward;
                    } else {
                        leftJoystickPosition = JoystickPositions.backwards;
                    }
                } else {
                    leftJoystickPosition = JoystickPositions.center;
                }
            }
        }, 500);

        mTextViewAngleRight = (TextView) findViewById(R.id.textView_angle_right);
        mTextViewStrengthRight = (TextView) findViewById(R.id.textView_strength_right);

        JoystickView joystickRight = (JoystickView) findViewById(R.id.joystickView_right);
        joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                mTextViewAngleRight.setText(angle + "°");
                mTextViewStrengthRight.setText(strength + "%");
                if (strength > 10) {
                    if (angle < 180) {
                        rightJoystickPosition = JoystickPositions.forward;
                    } else {
                        rightJoystickPosition = JoystickPositions.backwards;
                    }
                } else {
                    rightJoystickPosition = JoystickPositions.center;
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.joysticks_menu, menu);
        this.menu = menu;
        if (BLEConnectionManager.getInstance().isConnected ) {
            menu.getItem(0).setIcon(getDrawable(R.drawable.ic_bluetooth_connected_white_24dp));
        } else {
            menu.getItem(0).setIcon(getDrawable(R.drawable.ic_bluetooth_disabled_white_24dp));
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        unregisterReceiver(mReceiver);
        super.onBackPressed();
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

    private  enum JoystickPositions {
        forward,
        backwards,
        center
    }
}
