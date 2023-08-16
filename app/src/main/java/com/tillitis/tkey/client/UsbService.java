package com.tillitis.tkey.client;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;

public class UsbService {
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serialDevice;
    private static final int BAUD_RATE = 62500;

    public UsbService(Activity activity) {
        usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
    }

    //Device seems to connect properly. No issues with method.
    public boolean connectDevice() {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            for (UsbDevice device : usbDevices.values()) {
                this.device = device;
                connection = usbManager.openDevice(device);
                serialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection);
                if (serialDevice != null) {
                    if (serialDevice.syncOpen()) {
                        serialDevice.setBaudRate(BAUD_RATE);
                        serialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
                        serialDevice.setStopBits(UsbSerialInterface.STOP_BITS_1);
                        serialDevice.setParity(UsbSerialInterface.PARITY_NONE);
                        serialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    //Might somewhat work, as TKey blinks red when attempting to load an app.
    public void writeData(byte[] data) {
        if (serialDevice != null && data != null) {
            serialDevice.syncWrite(data,0);
        }
    }

    //Doesn't work, returns 0.
    public byte[] readData(int length) {
        if (serialDevice != null) {
            byte[] buffer = new byte[length];
            int bytesRead = serialDevice.syncRead(buffer, 0);
            if (bytesRead > 0) {
                return buffer;
            }
        }
        return null;
    }
}
