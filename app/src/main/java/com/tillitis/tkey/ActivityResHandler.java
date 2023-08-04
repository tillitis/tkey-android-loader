package com.tillitis.tkey;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import java.io.InputStream;

public class ActivityResHandler {
    private ActivityResultLauncher<Intent> resultLauncher;

    public ActivityResHandler(Fragment fragment, ResultCallback callback) {
        setupResultLauncher(fragment, callback);
    }

    private void setupResultLauncher(Fragment fragment, ResultCallback callback) {
        resultLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            try {
                                InputStream inputStream = fragment.getActivity().getContentResolver().openInputStream(uri);
                                byte[] fileBytes = HelperMethods.readBytes(inputStream);
                                callback.onResult(fileBytes);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });
    }

    public ActivityResultLauncher<Intent> getResultLauncher() {
        return resultLauncher;
    }

    public interface ResultCallback {
        void onResult(byte[] fileBytes);
    }

}
