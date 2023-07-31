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
import com.tillitis.tkey.client.signer.TK1sign;

public class SignerFragment extends Fragment {
    private MainActivity mainActivity;
    private CommonController cc;
    private SignerController sc;
    private final TK1sign signer;
    public SignerFragment(TkeyClient client) {
        signer = new TK1sign(client);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tkey_signer, container, false);
        mainActivity = (MainActivity) getActivity();

        cc = mainActivity.getCommonController();
        sc = new SignerController(cc);

        initializeButtons(view);
        return view;
    }

    private byte[] getFileBytes(){
        byte[] app = null;
        try{
            app = HelperMethods.readBytesFromAssets(mainActivity.getApplicationContext(),"signer.bin");
        }catch(Exception e){
            System.out.println(e);
        }
        return app;
    }

    private void initializeButtons(View view) {
        Button connectButton = view.findViewById(R.id.connect);
        Button loadApp = view.findViewById(R.id.loadApp);
        Button getPubKey = view.findViewById(R.id.getpubkey);

        connectButton.setOnClickListener(cc :: connectButtonOnClick);
        getPubKey.setOnClickListener(v -> sc.getPubKeyOnClick(v,signer));
        loadApp.setOnClickListener(v -> cc.loadAppOnClick(v, getFileBytes()));
    }
}

