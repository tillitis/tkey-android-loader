/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.tillitis.tkey.client;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import com.tillitis.tkey.MainActivity;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import java.io.IOException;
import java.util.List;

public class UsbComm {
    private Context context;
    private UsbSerialPort port;
    public UsbComm(Context context) {
        this.context = context;
    }

    public void connectDevice() {
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        // Find the first available driver.
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (!availableDrivers.isEmpty()) {
            UsbSerialDriver driver = availableDrivers.get(0);

            UsbDevice device = driver.getDevice();
            if (!manager.hasPermission(device)) {
                ((MainActivity) context).requestPermission(device);
            } else {
                UsbSerialPort port = driver.getPorts().get(0);
                try {
                    port.open(manager.openDevice(driver.getDevice()));
                    port.setParameters(62500, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                    this.port = port;
                } catch (IOException e) {
                    System.out.println("Error when connecting!");
                }
            }
        }
    }

    public void writeData(byte[] data) {
        if (port != null) {
            try {
                port.write(data, 1500);
            } catch (IOException e) {
                System.out.println("Write error");
            }
        }
    }

    private byte[] bufferStorage = new byte[1];

    public byte[] readData(int bytes) {
        byte[] buffer = new byte[bytes];
        byte[] expandedBuffer = new byte[bytes+1];
        byte[] finalBuffer = new byte[bytes];

        if (port != null) {
            try {
                port.read(buffer, 1000);
                bufferStorage[0] = buffer[buffer.length-1];
            } catch (IOException e) {
                System.out.println("Read error");
            }
        }
        expandedBuffer[0] = bufferStorage[0];
        System.arraycopy(buffer, 0, expandedBuffer, 1, buffer.length);

        System.arraycopy(expandedBuffer, 0, finalBuffer, 0, bytes);
        return finalBuffer;
    }

    /**
     * Clears read buffer and control lines to ensure no data remains in r/w buffers.
     */
    public void clear() throws IOException {
        byte[] buffer = new byte[129];
        int bytesRead;
        do {
            bytesRead = port.read(buffer, 250);
        } while (bytesRead > 0);
        port.getControlLines().clear();
        port.getSupportedControlLines().clear();
    }

    public boolean isConnected() {
        return (this.port != null);
    }
}

