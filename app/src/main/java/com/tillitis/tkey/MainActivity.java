/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.tillitis.tkey;
import android.app.Application;
import android.content.*;
import android.hardware.usb.*;
import androidx.fragment.app.*;

import com.fazecast.jSerialComm.SerialPort;
import com.tillitis.tkey.client.*;
import com.tillitis.tkey.fragments.*;
import android.app.PendingIntent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.tillitis.tkey.controllers.CommonController;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.iknek.tkey.USB_PERMISSION";
    private SerialPort usbComm;
    private TkeyClient tkeyClient;
    private UsbManager usbManager;
    private PendingIntent permissionIntent;
    private CommonController commonController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tkeyClient = new TkeyClient();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);

        commonController = new CommonController(findViewById(R.id.response_msg), findViewById(R.id.scroll), tkeyClient);

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

            if (itemId == R.id.action_tkey_tools)
                selectedFragment = new ToolsFragment();
             else if (itemId == R.id.action_signer)
                selectedFragment = new SignerFragment(tkeyClient);
             else if (itemId == R.id.action_verify)
                selectedFragment = new VerifyFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment).commit();
            return true;
        });
    }

    /**
     * USB Reciever for handling when a TKey is plugged in.
     */
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            try {
                                tkeyClient.connect();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            commonController.setConnected(true);
                        }
                    } else {
                        System.out.println("Permission denied for device " + device);
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Application app = getApplication();

                SerialPort.setAndroidContext(app);
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null && !usbManager.hasPermission(device)) {
                    usbManager.requestPermission(device, permissionIntent);
                    commonController.setConnected(false);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                commonController.setConnected(false);
                commonController.setLoaded(false);
                if (device != null) {
                    View view = getWindow().getDecorView().findViewById(android.R.id.content);
                    Snackbar.make(view, "TKey disconnected!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        }
    };

    public TkeyClient getClient(){
        return tkeyClient;
    }

    public void requestPermission(UsbDevice device) {
        usbManager.requestPermission(device, permissionIntent);
    }

    public CommonController getCommonController() {
        return commonController;
    }
}
