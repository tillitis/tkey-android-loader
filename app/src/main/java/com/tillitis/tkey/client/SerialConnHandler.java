/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.tillitis.tkey.client;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.android.AndroidPort;
import com.tillitis.tkey.MainActivity;

import static com.fazecast.jSerialComm.SerialPort.*;

public class SerialConnHandler {

    private static SerialPort conn;

    private static int speed;

    private static Boolean hasCon;

    protected SerialConnHandler(){

        speed = 62500;

        conn = AndroidPort.getCommPortsNative()[0];
    }

    public boolean getHasCon(){
        return hasCon;
    }

    /**
     * Currently not functioning. To be done.
     */
    void reconnect(){
        conn = SerialPort.getCommPorts()[0];
        conn.openPort();
        portListener();
        hasCon = true;
    }

    protected void connect() throws Exception {
        conn.setBaudRate(speed);
        conn.openPort();
        setReadTimeout();
        if (!conn.openPort()) {
            throw new Exception("Unable to open port " + conn.getSystemPortName());
        }
        else{
            System.out.println("Port opened!");
            portListener();
            hasCon = true;
        }
    }

    void flush(){
        conn.clearRTS();
        conn.clearDTR();
        conn.flushIOBuffers();
    }

    private void portListener(){
        conn.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
            }
            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED){
                    hasCon = false;
                }
            }
        });
    }

    protected void setConn(String name){
        conn = SerialPort.getCommPort(name);
    }

    public void setSpeed(int speed){
        conn.setBaudRate(speed);
    }

    protected void closePort(){
        conn.closePort();
        conn.removeDataListener();
        conn = null;
    }

    public SerialPort getConn(){
        return conn;
    }

    protected void setReadTimeout(){
        try{
            conn.setComPortTimeouts(TIMEOUT_READ_SEMI_BLOCKING,0,0);
        }catch (Exception e){
            System.out.println("Failed to set read timeout with err: " + e);
        }
    }

    protected void setReadTimeout(int read, int write){
        try{
            conn.setComPortTimeouts(TIMEOUT_READ_BLOCKING,read,write);
        }catch (Exception e){
            System.out.println("Failed to set read timeout with err: " + e);
        }
    }

}
