/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.tillitis.tkey.controllers;
import com.tillitis.tkey.client.*;
import android.content.Intent;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import com.google.android.material.snackbar.Snackbar;

public class ToolsController {
    private CommonController cc;
    private TkeyClient tk;

    public ToolsController(CommonController commonController) {
        this.cc = commonController;
        tk = cc.getTkeyClient();
    }

    public void getTKNameOnClick(View v) {
        String rsp;
        try {
            tk.clearIO();
            String name = tk.getNameVersion();
            cc.appendText(name + "\n");
            rsp = "Got Name";
        } catch (Exception e) {
            rsp = "Failed to get name";
            cc.appendText(rsp + "\n");
        }
        Snackbar.make(v, rsp, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public void getUDIButtonOnClick(View v) {
        String name;
        try {
            tk.clearIO();
            UDI udi = tk.getUDI();
            cc.appendText("TKey UDI: 0x0" + Integer.toHexString(udi.getVendorID()) + "0" +
                    Integer.toHexString(udi.getUdi()[0]) + "00000" + Integer.toHexString(udi.getSerial()) + "\n"
                    + "Vendor ID: " + Integer.toHexString(udi.getVendorID()) + " Product ID: " + udi.getProductID()
                    + " Product Rev: " + udi.getProductRevision() + "\n");

            name = "Got UDI!";
        } catch (Exception e) {
            name = "Failed to get UDI";
            cc.appendText(name + "\n");
        }
        Snackbar.make(v, name, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }
}