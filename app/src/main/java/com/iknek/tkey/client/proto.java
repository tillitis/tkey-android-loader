package com.iknek.tkey.client;

import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialPort;

public class proto {
    /**
     * Pre-defined list of commands and responses used in TKey communication.
     */
    private static final FwCmd cmdGetNameVersion = new FwCmd(0x01, "cmdGetNameVersion", CmdLen.CmdLen1);
    private static final FwCmd rspGetNameVersion = new FwCmd(0x02, "rspGetNameVersion", CmdLen.CmdLen32);
    private static final FwCmd cmdLoadApp = new FwCmd(0x03, "cmdLoadApp", CmdLen.CmdLen128);
    private static final FwCmd rspLoadApp = new FwCmd(0x04, "rspLoadApp", CmdLen.CmdLen4);
    private static final FwCmd cmdLoadAppData = new FwCmd(0x05, "cmdLoadAppData", CmdLen.CmdLen128);
    private static final FwCmd rspLoadAppData = new FwCmd(0x06, "rspLoadAppData", CmdLen.CmdLen4);
    private static final FwCmd rspLoadAppDataReady = new FwCmd(0x07, "rspLoadAppDataReady", CmdLen.CmdLen128);
    private static final FwCmd cmdGetUDI = new FwCmd(0x08, "cmdGetUDI", CmdLen.CmdLen1);
    private static final FwCmd rspGetUDI = new FwCmd(0x09, "rspGetUDI", CmdLen.CmdLen32);
    /**
     * Parses Framing HDR from the passed in int (retrieved from reading 1 byte from TKey).
     */
    private FramingHdr parseFrame(int b) throws Exception {
        if ((b & 0b1000_0000) != 0) {
            throw new Exception("Reserved bit #7 is not zero");
        }
        boolean response = (b & 0b0000_0100) != 0;
        int id = (b & 0b0110_0000) >> 5;
        int endpoint = (b & 0b0001_1000) >> 3;
        CmdLen cmdLen = CmdLen.values()[b & 0b0000_0011];

        FramingHdr framingHdr = new FramingHdr(id,endpoint,cmdLen);
        framingHdr.setResponseNotOk(response);
        return framingHdr;
    }

    /**
     * NewFrameBuf allocates a buffer with the appropriate size for the
     * command in cmd, including the framing protocol header byte. The cmd
     * parameter is used to get the endpoint and command length, which
     * together with id parameter are encoded as the header byte. The
     * header byte is placed in the first byte in the returned buffer. The
     * command code from cmd is placed in the buffer's second byte.
     */
    protected byte[] newFrameBuf(FwCmd cmd, int id) throws Exception{
        CmdLen cmdlen = cmd.getCmdLen();

        validate(id, 0, 3, "Frame ID needs to be between 1..3");
        validate(cmd.getEndpoint(), 0, 3, "Endpoint must be 0..3");
        validate(cmdlen.getByteVal(), 0, 3, "cmdLen must be 0..3");

        byte[] tx = new byte[cmdlen.getBytelen()+1];
        tx[0] = (byte) ((id << 5) | (cmd.getEndpoint() << 3) | cmdlen.getByteVal());
        tx[1] = (byte) cmd.getCode();
        return tx;
    }


    /**
     * dump(string, int[]) hexdumps data in d with an explaining string s first. It
     * expects d to contain the whole frame as sent on the wire, with the
     * framing protocol header in the first byte.
     */
    protected void dump(String s, byte[] d) throws Exception {
        if(d == null || d.length == 0){
            throw new Exception("No data!");
        }
        FramingHdr framingHdr = parseFrame(d[0]);
        System.out.println(s + " frame len: " + framingHdr.getCmdLen().getBytelen() + " bytes");
    }

    /**
     * ReadFrame reads a response in the framing protocol. The header byte
     * is first parsed. If the header has response status Not OK,
     * ErrResponseStatusNotOK is returned. Header command length and
     * endpoint are then checked against the expectedResp parameter,
     * header ID is checked against expectedID. The response code (first
     * byte after header) is also checked against the code in
     * expectedResp. It returns the whole frame read, and the parsed header
     * byte if successful.
     */

    protected void write(byte[] d, UsbComm con) throws Exception {
        try{
            con.writeData(d);
        }catch(Exception e){
            throw new Exception("Couldn't write" + e);
        }
    }

    protected byte[] readFrame(FwCmd expectedResp, int expectedID, UsbComm con) throws Exception {
        byte eEndpoint = expectedResp.getEndpoint();
        validate(expectedID, 0, 3, "Frame ID needs to be between 1..3");
        validate(eEndpoint, 0, 3, "Endpoint must be 0..3");
        validate(expectedResp.getCmdLen().getByteVal(), 0, 3, "cmdLen must be 0..3");

        byte[] rxHdr = new byte[2];
        try{
            rxHdr = con.readData(2);
        }catch(Exception e){
            throw new Exception("Read failed, error: " + e);
        }
        FramingHdr hdr;
        try{
            hdr = parseFrame(rxHdr[1]);
        }catch(Exception e){
            throw new Exception("Couldn't parse framing header. Failed with error: " + e);
        }
        if(hdr.getResponseNotOk()){
            con.readData(hdr.getCmdLen().getBytelen());
            throw new Exception("Response status not OK");
        }
        if(hdr.getCmdLen() != expectedResp.getCmdLen()){

            System.out.println("Expected cmdlen " + expectedResp.getCmdLen() + " , got" + hdr.getCmdLen());
        }
        if(hdr.getID() != expectedID){
            System.out.println("miss-match ID");
            System.out.println("real id: " + hdr.getID());
            System.out.println("expected id: " + expectedID);
        }
        if(hdr.getEndpoint() != eEndpoint){
            System.out.println("miss-match endpoint");
            System.out.println("real end: " + hdr.getEndpoint());
            System.out.println("expected end: " + eEndpoint);
        }
        //validate(hdr.getEndpoint(), eEndpoint, eEndpoint, "Msg not meant for us, dest: " + hdr.getEndpoint());
        //validate(hdr.getID(), expectedID, expectedID, "Expected ID: " + expectedID + " got: " + hdr.getID());

        byte[] rx = new byte[1+(expectedResp.getCmdLen().getBytelen())];
        rx[0] = rxHdr[0];
        int eRespCode = expectedResp.getCode();
        if(rx[0] != eRespCode){
            System.out.println("Expected cmd code 0x" + eRespCode + ", got 0x" + rx[1]);
            System.out.println("If this happens more than once during app loading, check device app and restart is recommended!");
        }
        try{
            Thread.sleep(10);
            rx = con.readData(rx.length);
        } catch(Exception e){
            throw new Exception("Read failed, error: " + e);
        }
        return rx;
    }

    /**
     * Helper method for validating values.
     */
    private void validate(int value, int min, int max, String errorMessage) throws Exception {
        if (value < min || value > max) throw new Exception(errorMessage);
    }

    FwCmd[] getAllCommands() {
        return new FwCmd[]{
                cmdGetNameVersion,
                rspGetNameVersion,
                cmdLoadApp,
                rspLoadApp,
                cmdLoadAppData,
                rspLoadAppData,
                rspLoadAppDataReady,
                cmdGetUDI,
                rspGetUDI
        };
    }

    public FwCmd getCmdGetNameVersion() {
        return cmdGetNameVersion;
    }

    public FwCmd getRspGetNameVersion() {
        return rspGetNameVersion;
    }

    public FwCmd getCmdLoadApp() {
        return cmdLoadApp;
    }

    public FwCmd getRspLoadApp() {
        return rspLoadApp;
    }

    public FwCmd getCmdLoadAppData() {
        return cmdLoadAppData;
    }

    public FwCmd getRspLoadAppData() {
        return rspLoadAppData;
    }

    public FwCmd getRspLoadAppDataReady() {
        return rspLoadAppDataReady;
    }

    public FwCmd getCmdGetUDI() {
        return cmdGetUDI;
    }

    public FwCmd getRspGetUDI() {
        return rspGetUDI;
    }
}

