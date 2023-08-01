/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.tillitis.tkey.controllers;
import android.text.method.ScrollingMovementMethod;
import android.view.*;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.tillitis.tkey.client.TkeyClient;

public class CommonController {
    private TextView textView;
    private ScrollView sc;
    private TkeyClient tk;
    private boolean isConnected = false;
    private boolean appIsLoaded = false;

    public CommonController(TextView tv, ScrollView sc, TkeyClient tk) {
        this.textView = tv;
        this.sc = sc;
        this.tk = tk;
        textView.setMovementMethod(new ScrollingMovementMethod());
    }

    public void appendText(String text){
        textView.append(text + "\n");
        sc.scrollTo(0,sc.getBottom());
        //sc.fullScroll(View.FOCUS_DOWN);
    }

    public void loadAppOnClick(View v, byte[] app){
        if(!appIsLoaded){
            textView.append("Loading app... \n ");
            try{
                Thread.sleep(50);
                tk.LoadApp(app);
                appendText("App Loaded \n");
                appIsLoaded = true;
            }catch (Exception e){
                String rsp = "Failed to load";
                Snackbar.make(v, rsp, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
        else{
            Snackbar.make(v, "App Already Loaded", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    public void connectButtonOnClick(View v) {
        String name;
        if(!isConnected) {
            name = connectDevice();
        } else {
            name = "Already Connected!";
            appendText(name + "\n");
        }
        Snackbar.make(v, name, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    String connectDevice() {
        try {
            tk.connect();
            if (tk.isConnected()) {
                isConnected = true;
                appendText("Device Connected \n");

                return "Connected!";
            } else {
                appendText("Failed to connect! \n");

                isConnected = false;
                return "Failed!";
            }
        } catch (Exception e) {
            appendText("Failed to connect! \n");
            isConnected = false;
            return "Failed!";
        }
    }

    boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean appIsLoaded() {
        return appIsLoaded;
    }

    public void setLoaded(Boolean bool){
        appIsLoaded = bool;
    }

    TkeyClient getTkeyClient() {
        return tk;
    }

}
