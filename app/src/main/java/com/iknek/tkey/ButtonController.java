package com.iknek.tkey;

import android.content.Intent;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import com.iknek.tkey.client.TkeyClient;
import com.iknek.tkey.client.UDI;
import com.google.android.material.snackbar.Snackbar;
import com.iknek.tkey.client.tkey.TK1sign;

public class ButtonController {
    private final TextView textView;
    private final TkeyClient tkeyClient;
    private boolean isConnected;
    private boolean appIsLoaded = false;

    public ButtonController(TkeyClient tkeyClient, TextView textView) {
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

    void openFileButtonOnClick(ActivityResultLauncher resultLauncher){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        resultLauncher.launch(intent);
    }

    public void getAppNameOnClick(View v){
        TK1sign signer = new TK1sign(tkeyClient);
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

    public void loadAppOnClick(View v, TkeyClient tk, byte[] app){
        if(!appIsLoaded){
            new Thread(() -> {
                try{
                    tk.LoadApp(app);
                    Snackbar.make(v, "Loading App", Snackbar.LENGTH_LONG).setAction("Action", null).show();
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
            String a = "Serial: " + udi.getSerial();
            textView.append(a + "\n" + "\n");
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