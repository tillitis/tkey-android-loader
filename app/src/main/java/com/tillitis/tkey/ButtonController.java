/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.tillitis.tkey;
import android.content.Intent;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import com.tillitis.tkey.client.TkeyClient;
import com.tillitis.tkey.client.UDI;
import com.google.android.material.snackbar.Snackbar;
import com.tillitis.tkey.client.signer.TK1sign;

import java.util.Arrays;

public class ButtonController {
    private final TextView textView;
    private final TkeyClient tkeyClient;
    private static boolean isConnected;
    private boolean appIsLoaded = false;

    public ButtonController(TkeyClient tkeyClient, TextView textView) {
        this.tkeyClient = tkeyClient;
        this.textView = textView;
        textView.setMovementMethod(new ScrollingMovementMethod());
        snapText();
    }

    private void snapText() {
        textView.post(() -> {
            textView.setGravity(Gravity.TOP | Gravity.START);
            textView.scrollTo(0, 0);
        });
    }
    public static void setConnectionStatus(boolean status){
        isConnected = status;
    }

    public void connectButtonOnClick(View v) {
        String name;
        if(!isConnected) {
            name = connectDevice();
        } else {
            name = "Already Connected!";
            textView.append(name + "\n" + "\n");
            snapText();
        }
        Snackbar.make(v, name, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    void openFileButtonOnClick(ActivityResultLauncher resultLauncher){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        resultLauncher.launch(intent);
    }

    public void getAppNameOnClick(View v, TK1sign signer){
        if(!appIsLoaded){
            Snackbar.make(v, "Load app first!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
        else{
            try{
                String name = signer.getAppNameVersion();
                textView.append("Loaded App \n" + "App Name: " + name + "\n" + "\n");
                snapText();
            }catch (Exception e){
                Snackbar.make(v, "Failed to load app", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
    }

    public void getPubKeyOnClick(final View v, final TK1sign signer){

        new Thread(() -> {

            try {
                Thread.sleep(500);
                String name = signer.getAppNameVersion();
                textView.append("Name: " + name + "\n\n");
                snapText();

                String key = Arrays.toString(signer.getPubKey());
                textView.append("KEY: " + key + "\n\n");
                snapText();
                textView.append("Public key received" + "\n" + "\n");
                snapText();
            } catch (Exception e) {
                final String rsp = "Failed to get public key";
                System.out.println(rsp);

                v.post(() -> Snackbar.make(v, rsp, Snackbar.LENGTH_LONG).setAction("Action", null).show());
            }
        }).start();
    }

    public void loadAppOnClick(View v, TkeyClient tk, byte[] app){
        if(!appIsLoaded){
            new Thread(() -> {
                try{
                    textView.append("Loading app..." + "\n" + "\n");
                    snapText();
                    tk.LoadApp(app);
                    textView.append("App Loaded" + "\n" + "\n");
                    snapText();
                    appIsLoaded = true;
                }catch (Exception e){
                    String rsp = "Failed to load";
                    System.out.println(rsp);
                    Snackbar.make(v, rsp, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }).start();
        }
        else{
            Snackbar.make(v, "App Already Loaded", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    public void getNameButtonOnClick(View v) {
        String rsp;
        try {
            tkeyClient.clearIO();
            String name = tkeyClient.getNameVersion();
            textView.append(name + "\n" + "\n");
            snapText();
            rsp = "Got Name";
        } catch (Exception e) {
            rsp = "Failed to get name";
            textView.append(rsp + "\n" + "\n");
            snapText();
        }
        Snackbar.make(v, rsp, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public void getUDIButtonOnClick(View v) {
        String name;
        try {
            tkeyClient.clearIO();
            UDI udi = tkeyClient.getUDI();
            textView.append("TKey UDI: 0x0" + Integer.toHexString(udi.getVendorID()) + "0" + Integer.toHexString(udi.getUdi()[0]) + "00000" + Integer.toHexString(udi.getSerial()) + "\n");
            textView.append("Vendor ID: " + Integer.toHexString(udi.getVendorID()) + " Product ID: " + udi.getProductID() + " Product Rev: " + udi.getProductRevision() + "\n" + "\n");
            snapText();
            name = "Got UDI!";
        } catch (Exception e) {
            name = "Failed to get UDI";
            textView.append(name + "\n" + "\n");
            snapText();
        }
        Snackbar.make(v, name, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    private String connectDevice() {
        try {
            tkeyClient.connect();
            if (tkeyClient.isConnected()) {
                isConnected = true;
                textView.append("Device Connected \n" + "\n");
                snapText();
                return "Connected!";
            } else {
                textView.append("Failed to connect! \n" + "\n");
                snapText();
                isConnected = false;
                return "Failed!";
            }
        } catch (Exception e) {
            textView.append("Failed to connect! \n" + "\n");
            snapText();
            isConnected = false;
            return "Failed!";
        }
    }
}