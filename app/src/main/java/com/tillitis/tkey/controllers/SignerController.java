/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.tillitis.tkey.controllers;
import android.view.View;
import com.google.android.material.snackbar.Snackbar;
import com.tillitis.tkey.ActivityResHandler;
import com.tillitis.tkey.client.TK1sign;
import com.tillitis.tkey.fragments.SignerFragment;

import java.util.Arrays;

public class SignerController {
    private CommonController cc;
    private String appName = "";
    private String pubKey = "";
    private TK1sign signer;
    private View v;

    public SignerController(CommonController commonController, TK1sign signer,View view) {
        this.signer = signer;
        this.cc = commonController;
        this.v = view;
    }

    /**
     * This is a bad way to do this. If for some reason the public key is incorrect,
     * users have no way of knowing, and it can only be re-done by reloading the client app.
     * Needed to get pub key though...
     */
    public void getPubKeyOnClick(){
        if(appName.equals("")){
            try{
                appName = signer.getAppNameVersion();
                Thread.sleep(10);
            }catch (Exception e){
                System.out.println("hi");
            }
        }
        else {
            try {
                if(pubKey.equals("")){
                    pubKey = signer.bytesToHex(signer.getPubKey());
                    cc.appendText("Key: " + pubKey + "\n");
                    cc.appendText("Public key received \n");
                }
                else{
                    cc.appendText("Cached Key: " + pubKey + "\n");
                }
            } catch (Exception e) {
                final String rsp = "Failed to get public key";
                System.out.println(rsp);
                v.post(() -> Snackbar.make(v, rsp, Snackbar.LENGTH_LONG).setAction("Action", null).show());
            }
        }
    }

    public void loadApp(byte[] data) {
        cc.loadAppOnClick(v,data);
        try{
            appName = signer.getAppNameVersion();
            cc.appendText("App Name: " + appName + "\n");

        }catch (Exception e){
            System.out.println("oops");
        }
    }

    public void getNameOnClick() {
        String rsp;
        try {
            String name = signer.getAppNameVersion();
            cc.appendText("App name: " + name + "\n");
            rsp = "Got Name";
        } catch (Exception e) {
            rsp = "Failed to get name";
            cc.appendText(rsp + "\n");
        }
        Snackbar.make(v, rsp, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public void signFile(byte[] data) throws Exception {
        if(data.length > 4096){
            cc.appendText("File too large to sign! \n");
            throw new Exception("too large");
        }
        byte[] sig = signer.sign(data);
        cc.appendText("Signature over message by TKey (on stdout): " + " \n" + signer.bytesToHex(sig) +  " \n");
    }


}
