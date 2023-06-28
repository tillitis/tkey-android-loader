package com.iknek.tkey.client;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static com.iknek.tkey.client.ArrayConverter.byteArrayToIntArray;
import static com.iknek.tkey.client.ArrayConverter.bytesToUnsignedBytes;
import static com.iknek.tkey.client.ArrayConverter.intArrayToByteArray;

public class TkeyClient {
    private static final proto proto = new proto();
    private UsbComm connHandler;
    private static final int ID = 2;

    public void main(UsbComm com){
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
        int[] deviceDigest = new int[32];

        for(int nsent; offset < binLen; offset+= nsent){
            Tuple tup;
            try{
                if(binLen-offset <= proto.getCmdLoadAppData().getCmdLen().getBytelen()-1){
                    tup = loadAppData((Arrays.copyOfRange(bin, offset, bin.length)),true);
                    deviceDigest = tup.getIntArray();
                    nsent = tup.getIntValue();
                } else {
                    tup = loadAppData(Arrays.copyOfRange(bin, offset, bin.length),false);
                    nsent = tup.getIntValue();
                }
            }catch(Exception e){
                throw new Exception("loadAppData error: " + e);
            }
        }
        if(offset > binLen) throw new Exception("Transmitted more than expected");

        //byte[] digest = new byte[32];
        //Blake2s256 blake2s = new Blake2s256();
        //digest = blake2s.digest(bin);
        /*Blake2s blake2s = new Blake2s(digest.length);
        digest = blake2s.digest(bin);
        if (!Arrays.equals(intArrayToByteArray(deviceDigest), digest)) {
            System.out.println(Arrays.toString(deviceDigest));
            System.out.println(Arrays.toString(digest));
            throw new Exception("Different digests");
        }
        System.out.println("Same digests!");*/
    }

    /**
     * loadApp() sets the size and USS of the app to be loaded into the TKey.
     */
    private void loadApp(int size, byte[] secretPhrase) throws Exception {
        int[] tx = proto.newFrameBuf(proto.getCmdLoadApp(),ID);
        tx[2] = size;
        tx[3] = size >> 8;
        tx[4] = size >> 16;
        tx[5] = size >> 24;
        tx[6] = 0;
        /*
        if(secretPhrase.length == 0){
        }else{
            byte[] uss = null;
        }*/
        // TODO: Implement blake2s and USS
        try{
            proto.dump("LoadApp tx", tx);
        }catch(Exception e){
            throw new Exception(e);
        }
        byte[] tx_arr = intArrayToByteArray(tx);
        try{
            connHandler.writeData(tx_arr);
        }catch(Exception e){
            throw new Exception(e);
        }
    }

    /**
     * loadAppData() loads a chunk of the raw app binary into the TKey.
     */
    private Tuple loadAppData(byte[] contentByte, boolean last) throws Exception {
        int[] tx = proto.newFrameBuf(proto.getCmdLoadAppData(), ID);

        int[] payload = new int[proto.getCmdLoadAppData().getCmdLen().getBytelen()-1];
        int copied = Math.min(contentByte.length, payload.length);
        System.arraycopy(byteArrayToIntArray(contentByte), 0, payload, 0, copied);

        if (copied < payload.length) {
            int[] padding = new int[payload.length - copied];
            System.arraycopy(padding, 0, payload, copied, padding.length-1); //this line does nothing.
        }
        System.arraycopy(payload, 0, tx, 2, payload.length);
        try{
            proto.dump("LoadAppData tx", tx);
        }catch (Exception e){
            throw new Exception(e);
        }

        connHandler.writeData(intArrayToByteArray(tx));

        FwCmd cmd;
        if(last){
            cmd = proto.getRspLoadAppDataReady();
        } else cmd = proto.getRspLoadAppData();

        byte[] rx;
        try {
            Thread.sleep(10);
            rx = proto.readFrame(cmd, ID, connHandler, last);
        }catch (Exception e){
            throw new Exception(e);
        }
        if(last){
            int[] digest = new int[32];
            System.arraycopy(byteArrayToIntArray(rx), 3, digest,0 ,32);
            return new Tuple(digest,copied);
        }
        return new Tuple(new int[32], copied);
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
    private String unpackName(byte[] raw) {
        String name0 = new String(raw, 1, 4);
        String name1 = new String(raw, 5, 4);
        long version = ByteBuffer.wrap(raw, 9, 4).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xffffffffL;
        String concated = name0 + name1 + " " + version;
        System.out.println("TKey Device name and version: " + concated);
        return concated;
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

    /**
     * getData is used in both getNameVersion and getUDI to send instructions and receive
     * replies + data from the TKey.
     */
    private byte[] getData(FwCmd command, FwCmd response) throws Exception {
        byte[] tx_byte = intArrayToByteArray(proto.newFrameBuf(command, ID));
        connHandler.writeData(tx_byte);
        return proto.readFrame(response, 2, connHandler);
    }

    /**
     * Method that can be used to send and receive frames from a TKey.
     */
    public byte[] frameCom(FwCmd command, FwCmd eResp, int id, int eId, int[] data) throws Exception {
        int[] tx = proto.newFrameBuf(command, id);
        int[] payload = new int[command.getCmdLen().getBytelen()-1];
        int copied = Math.min(data.length, payload.length);
        System.arraycopy(data, 0, payload, 0, copied);

        if (copied < payload.length) {
            int[] padding = new int[payload.length - copied];
            System.arraycopy(padding, 0, payload, copied, padding.length-1); //this line does nothing.
        }
        System.arraycopy(payload, 0, tx, 2, payload.length);
        connHandler.writeData(intArrayToByteArray(tx));

        return proto.readFrame(eResp, eId, connHandler);
    }

    public static FwCmd[] getCommands(){
        return proto.getAllCommands();
    }


    private static byte[] readFile(String fileName) throws IOException {
        return java.nio.file.Files.readAllBytes(new File(fileName).toPath());
    }

}