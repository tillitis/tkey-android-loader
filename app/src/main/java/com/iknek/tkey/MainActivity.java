package com.iknek.tkey;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iknek.tkey.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;
import com.iknek.tkey.client.TkeyClient;
import com.iknek.tkey.client.UsbComm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.example.tkey.USB_PERMISSION";
    private ActivityMainBinding binding;
    private UsbComm usbComm;
    private TkeyClient tkeyClient;
    private UsbManager usbManager;
    private PendingIntent permissionIntent;
    private View viewForSnackbar;
    private ButtonController buttonController;
    private ActivityResultLauncher<Intent> resultLauncher;
    private byte[] fileBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        initializeUsbCommunication();

        // Com to USB.
        usbComm = new UsbComm(this);
        tkeyClient = new TkeyClient();
        tkeyClient.main(usbComm);


        TextView textView = findViewById(R.id.response_msg);
        buttonController = new ButtonController(tkeyClient, textView);
        initializeButtons();

        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            try {
                                InputStream inputStream = getContentResolver().openInputStream(uri);
                                fileBytes = readBytes(inputStream);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });
    }

    private void setupFragments() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.action_tkey_tools) {
                selectedFragment = new ToolsFragment();
            } else if (itemId == R.id.action_signer) {
                selectedFragment = new SignerFragment();
            } else if (itemId == R.id.action_verify) {
                selectedFragment = new VerifyFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }

            return true;
        });
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void initializeUsbCommunication() {
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        this.registerReceiver(usbReceiver, filter);
        viewForSnackbar = findViewById(android.R.id.content);
    }

    private void initializeButtons() {
        Button connectButton = findViewById(R.id.connect);
        Button getNameButton = findViewById(R.id.getName);
        Button getUDI = findViewById(R.id.getUDI);
        Button btnOpenFile = findViewById(R.id.btnOpenFile);
        Button getAppName = findViewById(R.id.getAppName);
        Button loadApp = findViewById(R.id.loadApp);

        connectButton.setOnClickListener(buttonController::connectButtonOnClick);
        getAppName.setOnClickListener(buttonController::getAppNameOnClick);
        getNameButton.setOnClickListener(buttonController::getNameButtonOnClick);
        getUDI.setOnClickListener(buttonController::getUDIButtonOnClick);
        loadApp.setOnClickListener(v -> buttonController.loadAppOnClick(v, tkeyClient, fileBytes));
        btnOpenFile.setOnClickListener(v -> buttonController.openFileButtonOnClick(resultLauncher));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            usbComm.connectDevice();
                            buttonController.setConnectionStatus(false);
                        }
                    } else {
                        System.out.println("Permission denied for device " + device);
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null && !usbManager.hasPermission(device)) {
                    usbManager.requestPermission(device, permissionIntent);
                    buttonController.setConnectionStatus(false);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    buttonController.setConnectionStatus(false);
                    Snackbar.make(viewForSnackbar, "TKey disconnected!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        }
    };

    public void requestPermission(UsbDevice device) {
        usbManager.requestPermission(device, permissionIntent);
    }
}
