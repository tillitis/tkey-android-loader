package com.iknek.tkey;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeUsbCommunication();

        // Com to USB.
        usbComm = new UsbComm(this);
        tkeyClient = new TkeyClient();
        tkeyClient.main(usbComm);

        TextView textView = findViewById(R.id.response_msg);
        buttonController = new ButtonController(this, usbComm, tkeyClient, textView);
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
                                byte[] fileBytes = readBytes(inputStream);
                                tkeyClient.getUDI();
                                Thread.sleep(500);
                                tkeyClient.LoadApp(fileBytes);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
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

        connectButton.setOnClickListener(buttonController::connectButtonOnClick);
        getNameButton.setOnClickListener(buttonController::getNameButtonOnClick);
        getUDI.setOnClickListener(buttonController::getUDIButtonOnClick);
        btnOpenFile.setOnClickListener(this::openFileButtonOnClick);
    }

    private void openFileButtonOnClick(View v){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        resultLauncher.launch(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
