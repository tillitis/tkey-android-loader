/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.tillitis.tkey.client;
import android.hardware.usb.*;
import com.hoho.android.usbserial.driver.*;
import java.util.*;
import com.tillitis.tkey.*;
import android.content.Context;
import java.io.IOException;

public class SerialPort {
    private Context context;
    private UsbSerialPort port;
    public SerialPort(Context context) {
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
                port.write(data, 100);
            } catch (IOException e) {
                System.out.println("Write error");
            }
        }
    }

    public void writeData(byte[] data, int len) {
        writeData(data);
    }

    public void disconnect() throws IOException {
        port.setBreak(true);

    }

    private byte[] bufferStorage = new byte[1];

    //TODO: might overflow if bytes back is more than expected
    public byte[] readData(int bytes) {
        if(bytes == 1 && port != null){
            byte[] buffer = new byte[bytes+1];
            try {
                port.read(buffer, 100);
                bufferStorage[0] = buffer[1];
            } catch (IOException e) {
                System.out.println("Read error");
            }
            return Arrays.copyOfRange(buffer,0,1);
        }
        else{
            byte[] buffer = new byte[bytes];
            byte savedbyte = bufferStorage[0];
            try {
                port.read(buffer, 100);
                bufferStorage[0] = buffer[bytes-1];
            } catch (IOException e) {
                System.out.println("Read error");
            }
            byte[] expandedbuffer = new byte[bytes];
            System.arraycopy(buffer,0,expandedbuffer,1,buffer.length-1);
            expandedbuffer[0] = savedbyte;
            return expandedbuffer;
        }
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

