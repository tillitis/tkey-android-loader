/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.tillitis.tkey.fragments;
import com.tillitis.tkey.*;
import com.tillitis.tkey.controllers.*;
import android.view.*;
import android.os.Bundle;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import com.tillitis.tkey.client.TkeyClient;
import com.tillitis.tkey.client.TK1sign;

public class SignerFragment extends Fragment {
    private MainActivity mainActivity;
    private CommonController cc;
    private SignerController sc;
    private final TK1sign signer;
    private ActivityResHandler resultHandler;

    private byte[] fileBytes;

    public SignerFragment(TkeyClient client) {
        signer = new TK1sign(client);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tkey_signer, container, false);
        mainActivity = (MainActivity) getActivity();

        cc = mainActivity.getCommonController();
        sc = new SignerController(cc, signer, view);
        initializeButtons(view);

        resultHandler = new ActivityResHandler(this, fileBytes -> this.fileBytes = fileBytes);

        return view;
    }

    private byte[] readFileBytes(){
        byte[] app = null;
        try{
            app = HelperMethods.readBytesFromAssets(mainActivity.getApplicationContext(),"signer.bin");
        }catch(Exception e){
            System.out.println(e);
        }
        return app;
    }

    public byte[] getFileBytes(){
        return fileBytes;
    }

    private void initializeButtons(View view) {
        Button connectButton = view.findViewById(R.id.connect);
        Button loadApp = view.findViewById(R.id.loadApp);
        Button getPubKey = view.findViewById(R.id.getpubkey);
        Button signFile = view.findViewById(R.id.signFile);
        Button getFile = view.findViewById(R.id.getFile);

        connectButton.setOnClickListener(cc :: connectButtonOnClick);
        getPubKey.setOnClickListener(v -> sc.getPubKeyOnClick());
        loadApp.setOnClickListener(v -> sc.loadApp(readFileBytes()));

        getFile.setOnClickListener(v -> cc.openFileButtonOnClick(resultHandler.getResultLauncher()));

        signFile.setOnClickListener(v -> {
            try {
                sc.signFile(fileBytes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}

