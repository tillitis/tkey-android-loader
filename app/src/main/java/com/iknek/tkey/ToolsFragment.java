package com.iknek.tkey;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import com.iknek.tkey.client.TkeyClient;
import java.io.InputStream;

public class ToolsFragment extends Fragment {
    private MainActivity mainActivity;
    private ButtonController buttonController;
    private byte[] fileBytes;
    private ActivityResultLauncher<Intent> resultLauncher;
    private TkeyClient tk;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tkey_tools, container, false);
        mainActivity = (MainActivity) getActivity();

        tk = mainActivity.getClient();

        buttonController = mainActivity.getButtonController();
        initializeButtons(view);

        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            try {
                                InputStream inputStream = mainActivity.getContentResolver().openInputStream(uri);
                                fileBytes = HelperMethods.readBytes(inputStream);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });

        return view;
    }

    private void initializeButtons(View view) {
        Button connectButton = view.findViewById(R.id.connect);
        Button getNameButton = view.findViewById(R.id.getName);
        Button getUDI = view.findViewById(R.id.getUDI);
        Button btnOpenFile = view.findViewById(R.id.btnOpenFile);
        Button loadApp = view.findViewById(R.id.loadApp);

        connectButton.setOnClickListener(buttonController::connectButtonOnClick);
        getNameButton.setOnClickListener(buttonController::getNameButtonOnClick);
        getUDI.setOnClickListener(buttonController::getUDIButtonOnClick);
        loadApp.setOnClickListener(v -> buttonController.loadAppOnClick(v,tk, fileBytes));
        btnOpenFile.setOnClickListener(v -> buttonController.openFileButtonOnClick(resultLauncher));
    }
}
