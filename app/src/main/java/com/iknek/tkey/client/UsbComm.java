package com.iknek.tkey.client;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.iknek.tkey.MainActivity;
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
                port.write(data, 1000);
            } catch (IOException e) {
                System.out.println("ha ha. send error");
            }
        }
    }

    private byte[] bufferStorage = new byte[1];

    /*public byte[] readData(int bytes) {
        byte[] buffer = new byte[bytes];
        if (port != null) {
            try {
                Thread.sleep(100);
                port.read(buffer, 500);

                bufferStorage[0] = buffer[buffer.length-1];
            } catch (IOException e) {
                System.out.println("ha ha. error");

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return buffer;
    }*/

    public byte[] readData(int bytes) {
        byte[] buffer = new byte[bytes];
        byte[] expandedBuffer = new byte[bytes+1];
        byte[] finalBuffer = new byte[bytes];

        if (port != null) {
            try {
                Thread.sleep(100);
                port.read(buffer, 500);
                bufferStorage[0] = buffer[buffer.length-1];
            } catch (IOException e) {
                System.out.println("ha ha. error");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        expandedBuffer[0] = bufferStorage[0];
        System.arraycopy(buffer, 0, expandedBuffer, 1, buffer.length);

        System.arraycopy(expandedBuffer, 0, finalBuffer, 0, bytes);
        return finalBuffer;
    }

    public boolean isConnected() {
        return (this.port != null);
    }
}

