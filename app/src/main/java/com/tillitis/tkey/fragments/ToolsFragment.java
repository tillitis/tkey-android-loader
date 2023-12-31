/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.tillitis.tkey.fragments;

import android.view.*;
import com.tillitis.tkey.*;
import com.tillitis.tkey.controllers.*;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import java.io.InputStream;

public class ToolsFragment extends Fragment {
    private MainActivity mainActivity;
    private CommonController cc;
    private ToolsController tc;
    private ActivityResHandler resultHandler;
    private byte[] fileBytes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tkey_tools, container, false);
        mainActivity = (MainActivity) getActivity();

        cc = mainActivity.getCommonController();
        tc = new ToolsController(cc);

        initializeButtons(view);

        resultHandler = new ActivityResHandler(this, fileBytes -> {
            this.fileBytes = fileBytes;
        });

        return view;
    }

    private void initializeButtons(View view) {
        Button connectButton = view.findViewById(R.id.connect);
        Button getNameButton = view.findViewById(R.id.getName);
        Button getUDI = view.findViewById(R.id.getUDI);
        Button btnOpenFile = view.findViewById(R.id.btnOpenFile);
        Button loadApp = view.findViewById(R.id.loadApp);

        connectButton.setOnClickListener(cc::connectButtonOnClick);
        getNameButton.setOnClickListener(tc::getTKNameOnClick);
        getUDI.setOnClickListener(tc::getUDIButtonOnClick);
        loadApp.setOnClickListener(v -> cc.loadAppOnClick(v, fileBytes));
        btnOpenFile.setOnClickListener(v -> cc.openFileButtonOnClick(resultHandler.getResultLauncher()));
    }
}
