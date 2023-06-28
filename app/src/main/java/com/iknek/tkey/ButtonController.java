package com.iknek.tkey;

import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.iknek.tkey.client.TkeyClient;
import com.iknek.tkey.client.UDI;
import com.iknek.tkey.client.UsbComm;
import com.google.android.material.snackbar.Snackbar;

public class ButtonController {
    private MainActivity activity;
    private TextView textView;
    private UsbComm usbComm;
    private TkeyClient tkeyClient;
    private boolean isConnected;

    public ButtonController(MainActivity activity, UsbComm usbComm, TkeyClient tkeyClient, TextView textView) {
        this.activity = activity;
        this.usbComm = usbComm;
        this.tkeyClient = tkeyClient;
        this.textView = textView;
        textView.setMovementMethod(new ScrollingMovementMethod());
    }

    private void snapText(){
        textView.scrollTo(0, textView.getLayout().getLineTop(textView.getLineCount()) - textView.getHeight());
    }
    public void setConnectionStatus(boolean status){
        this.isConnected = status;
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

    public void getNameButtonOnClick(View v) {
        String rsp;
        try {
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
            UDI udi = tkeyClient.getUDI();
            String a = "Serial: " + udi.serial();
            textView.append(a);
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
            usbComm.connectDevice();
            if (usbComm.isConnected()) {
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