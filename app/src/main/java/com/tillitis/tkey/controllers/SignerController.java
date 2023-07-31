/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.tillitis.tkey.controllers;
import android.view.View;
import com.google.android.material.snackbar.Snackbar;
import com.tillitis.tkey.client.signer.TK1sign;

import java.util.Arrays;

public class SignerController {
    private CommonController cc;

    public SignerController(CommonController commonController) {
        this.cc = commonController;
    }

    public void getAppNameOnClick(View v, TK1sign signer){
        if(!cc.appIsLoaded()){
            Snackbar.make(v, "Load app first!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
        else{
            try{
                String name = signer.getAppNameVersion();
                cc.appendText("Loaded App \n" + "App Name: " + name + "\n");
            }catch (Exception e){
                Snackbar.make(v, "Failed to load app", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
    }

    public void getPubKeyOnClick(final View v, final TK1sign signer){
        new Thread(() -> {
            try {
                String key = Arrays.toString(signer.getPubKey());
                cc.appendText("KEY: " + key + "\n");
                cc.appendText("Public key received \n");
            } catch (Exception e) {
                final String rsp = "Failed to get public key";
                System.out.println(rsp);
                v.post(() -> Snackbar.make(v, rsp, Snackbar.LENGTH_LONG).setAction("Action", null).show());
            }
        }).start();
    }

}
