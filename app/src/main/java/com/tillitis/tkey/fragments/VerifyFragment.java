/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.tillitis.tkey.fragments;
import android.os.Bundle;
import android.view.*;
import androidx.fragment.app.Fragment;

import com.tillitis.tkey.R;

public class VerifyFragment extends Fragment {

    public VerifyFragment() {
        // Currently empty as this fragment (and associated functionality) have not been implemented.
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tkey_verify, container, false);
    }
}
