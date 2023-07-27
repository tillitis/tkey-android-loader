/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.tillitis.tkey;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.tillitis.tkey.client.TkeyClient;
import com.tillitis.tkey.client.SerialPort;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.iknek.tkey.USB_PERMISSION";
    private SerialPort serialPort;
    private TkeyClient tkeyClient;
    private UsbManager usbManager;
    private PendingIntent permissionIntent;
    private ButtonController buttonController ;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serialPort = new SerialPort(this);
        tkeyClient = new TkeyClient();
        tkeyClient.main(serialPort);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
        textView = findViewById(R.id.response_msg);
        buttonController = new ButtonController(tkeyClient, textView);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, filter);
        setupFragments();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new ToolsFragment());
        transaction.commit();
    }

    private void setupFragments() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.action_tkey_tools) {
                selectedFragment = new ToolsFragment();
            } else if (itemId == R.id.action_signer) {
                selectedFragment = new SignerFragment(tkeyClient);
            } else if (itemId == R.id.action_verify) {
                selectedFragment = new VerifyFragment();
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();

            return true;
        });
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            serialPort.connectDevice();
                            ButtonController.setConnectionStatus(false);
                        }
                    } else {
                        System.out.println("Permission denied for device " + device);
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null && !usbManager.hasPermission(device)) {
                    usbManager.requestPermission(device, permissionIntent);
                    buttonController.setConnectionStatus(false);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    buttonController.setConnectionStatus(false);
                    View view = getWindow().getDecorView().findViewById(android.R.id.content);
                    Snackbar.make(view, "TKey disconnected!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        }
    };

    TkeyClient getClient(){
        return tkeyClient;
    }

    public void requestPermission(UsbDevice device) {
        usbManager.requestPermission(device, permissionIntent);
    }

    public ButtonController getButtonController() {
        return buttonController;
    }
}
