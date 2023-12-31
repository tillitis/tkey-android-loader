/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.tillitis.tkey.client;
import java.io.IOException;
import java.nio.*;
import java.util.Arrays;
import org.bouncycastle.crypto.digests.Blake2sDigest;

public class TkeyClient {
    private static final proto proto = new proto();
    private SerialPort connHandler;
    private static final int ID = 2;

    public void main(SerialPort com){
        connHandler = com;
    }

    public void LoadApp(byte[] bin) throws Exception {
        LoadApp(bin,new byte[0]);
    }

    private void LoadApp(byte[] bin, byte[] uss) throws Exception {
        int binLen = bin.length;
        if (binLen > 102400) throw new Exception("File too big");

        loadApp(binLen, uss);
        int offset = 0;
        byte[] deviceDigest = new byte[32];

        for(int nsent; offset < binLen; offset+= nsent){
            Tuple tup;
            try{
                if(binLen-offset <= proto.getCmdLoadAppData().getCmdLen().getBytelen()-1){
                    tup = loadAppData((Arrays.copyOfRange(bin, offset, bin.length)),true);
                    deviceDigest = tup.getByteArray();
                }
                else tup = loadAppData(Arrays.copyOfRange(bin, offset, bin.length),false);
                nsent = tup.getIntValue();
            }catch(Exception e){
                throw new Exception("loadAppData error: " + e);
            }
        }
        if(offset > binLen) throw new Exception("Transmitted more than expected");

        byte[] digest = hash(bin);

        deviceDigest = Arrays.copyOfRange(deviceDigest,0,32);

        System.out.println("Host Digest: " + Arrays.toString(bytesToUnsignedBytes(digest)));
        System.out.println("Device Digest: " + Arrays.toString(bytesToUnsignedBytes(deviceDigest)));

        if (!Arrays.equals(digest, deviceDigest)) {
            System.out.println("Different digests!");
        }
        else{
            System.out.println("Same digests!");
        }
    }

    public static byte[] hash(byte[] bytes) {
        Blake2sDigest digest = new Blake2sDigest(256);
        digest.update(bytes, 0, bytes.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        return hash;
    }

    /**
     * loadApp() sets the size and USS of the app to be loaded into the TKey.
     */
    public byte[] newFrameBuf(FwCmd cmd, int ID) throws Exception {
        return proto.newFrameBuf(cmd,ID);
    }

    private void loadApp(int size, byte[] secretPhrase) throws Exception {
        byte[] tx = proto.newFrameBuf(proto.getCmdLoadApp(),ID);
        tx[2] = (byte) size;
        tx[3] = (byte) (size >> 8);
        tx[4] = (byte) (size >> 16);
        tx[5] = (byte) (size >> 24);

        if(secretPhrase.length == 0){
            tx[6] = 0;
        }else{
            byte[] uss = hash(secretPhrase);
            System.arraycopy(uss, 0, tx, 6, uss.length);
        }
        try{
            proto.dump("LoadApp tx", tx);
        }catch(Exception e){
            throw new Exception(e);
        }
        try{
            connHandler.writeData(tx);
        }catch(Exception e){
            throw new Exception(e);
        }

        byte[] rx = proto.readFrame(proto.getRspLoadApp(),ID,connHandler);
        if(rx[2] != 0){
            System.out.println("LoadApp Not OK");
        }
    }

    /**
     * loadAppData() loads a chunk of the raw app binary into the TKey.
     */
    private Tuple loadAppData(byte[] contentByte, boolean last) throws Exception {
        byte[] tx = proto.newFrameBuf(proto.getCmdLoadAppData(), ID);

        byte[] payload = new byte[proto.getCmdLoadAppData().getCmdLen().getBytelen()-1];
        int copied = Math.min(contentByte.length, payload.length);
        System.arraycopy(contentByte, 0, payload, 0, copied);

        if (copied < payload.length) {
            byte[] padding = new byte[payload.length - copied];
            System.arraycopy(padding, 0, payload, copied, padding.length-1); //this line does nothing.
        }
        System.arraycopy(payload, 0, tx, 2, payload.length);
        try{
            proto.dump("LoadAppData tx", tx);
        }catch (Exception e){
            throw new Exception(e);
        }
        connHandler.writeData(tx);
        FwCmd cmd;
        if(last) cmd = proto.getRspLoadAppDataReady();
        else     cmd = proto.getRspLoadAppData();

        byte[] rx;
        try {
            Thread.sleep(5);
            rx = proto.readFrame(cmd, ID, connHandler);
        }catch (Exception e){
            throw new Exception(e);
        }
        if(last){
            byte[] digest = new byte[128];
            System.arraycopy(rx, 2, digest,0 ,32);
            return new Tuple(digest,copied);
        }
        return new Tuple(new byte[32], copied);
    }

    /**
     * getNameVersion gets the name and version from the TKey firmware
     */
    public String getNameVersion() throws Exception {
        byte[] data = getData(proto.getCmdGetNameVersion(), proto.getRspGetNameVersion());
        return unpackName(data);
    }

    /**
     * Unpacks name and prints it to the console.
     * @return the concated string, which can be used elsewhere.
     */
    public static String unpackName(byte[] raw) {
        String name0 = new String(raw, 1, 4);
        String name1 = new String(raw, 5, 4);
        long version = ByteBuffer.wrap(raw, 9, 4).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xffffffffL;
        return name0 + name1 + " " + version;
    }

    /**
     * getUDI gets the UDI (Unique Device ID) from the TKey firmware, and returns
     * a UDI object.
     */
    public UDI getUDI() throws Exception {
        byte[] data = getData(proto.getCmdGetUDI(), proto.getRspGetUDI());
        return unpackUDI(data);
    }

    /**
     * unpackUDI unpacks the array of bytes, creating a UDI object with all relevant fields,
     * in addition to returning the entire array for use if needed.
     */
    private static UDI unpackUDI(byte[] byteArray){
        short[] udi = Arrays.copyOfRange(bytesToUnsignedBytes(byteArray),2,10);

        int vpr = (udi[3] << 24) | ((udi[2] & 0xFF) << 16) | ((udi[1] & 0xFF) << 8) | (udi[0] & 0xFF);
        int unnamed = (vpr >> 28) & 0xf;
        int vendorID = (vpr >> 12) & 0xffff;
        int productID = (vpr >> 6) & 0x3f;
        int productRevision = vpr & 0x3f;
        int serial = (udi[7] << 24) | ((udi[6] & 0xFF) << 16) | ((udi[5] & 0xFF) << 8) | (udi[4] & 0xFF);

        return new UDI(vpr,unnamed,vendorID,productID,productRevision,serial,udi);
    }

    static short[] bytesToUnsignedBytes(byte[] bytes) {
        short[] unsignedBytes = new short[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            unsignedBytes[i] = (short) (bytes[i] & 0xFF);
        }
        return unsignedBytes;
    }

    /**
     * getData is used in both getNameVersion and getUDI to send instructions and receive
     * replies + data from the TKey.
     */
    public byte[] getData(FwCmd command, FwCmd response) throws Exception {
        byte[] tx_byte = proto.newFrameBuf(command, ID);
        connHandler.writeData(tx_byte);
        Thread.sleep(10);
        return proto.readFrame(response, ID, connHandler);
    }

    public void write(byte[] tx){
        connHandler.writeData(tx);
    }

    public byte[] read(int bytes) throws IOException {
        return connHandler.readData(bytes);
    }

    public byte[] readFrame(FwCmd cmd, int id) throws Exception {
        return proto.readFrame(cmd, id,connHandler);
    }

    public void connect(){
        connHandler.connectDevice();
    }

    public boolean isConnected(){
        return connHandler.isConnected();
    }

    public void clearIO() throws IOException {
        connHandler.clear();
    }
}