/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.tillitis.tkey.client;
import static com.tillitis.tkey.client.CmdLen.*;

public class proto {
    /**
     * Pre-defined list of commands and responses used in TKey communication.
     */
    private static final FwCmd cmdGetNameVersion = new FwCmd(0x01, "cmdGetNameVersion", CmdLen1);
    private final FwCmd rspGetNameVersion = new FwCmd(0x02, "rspGetNameVersion", CmdLen32);
    private final FwCmd cmdLoadApp = new FwCmd(0x03, "cmdLoadApp", CmdLen128);
    private final FwCmd rspLoadApp = new FwCmd(0x04, "rspLoadApp", CmdLen4);
    private final FwCmd cmdLoadAppData = new FwCmd(0x05, "cmdLoadAppData", CmdLen128);
    private final FwCmd rspLoadAppData = new FwCmd(0x06, "rspLoadAppData", CmdLen4);
    private final FwCmd rspLoadAppDataReady = new FwCmd(0x07, "rspLoadAppDataReady", CmdLen128);
    private final FwCmd cmdGetUDI = new FwCmd(0x08, "cmdGetUDI", CmdLen1);
    private final FwCmd rspGetUDI = new FwCmd(0x09, "rspGetUDI", CmdLen32);

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
     * Writes an array to the TKey, using the specified SerialPort.
     */
    protected void write(byte[] d, SerialPort con) throws Exception {
        try{
            con.writeData(d, d.length);
        }catch(Exception e){
            throw new Exception("Couldn't write" + e);
        }
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
    protected byte[] readFrame(FwCmd expectedResp, int expectedID, SerialPort con) throws Exception {
        byte eEndpoint = expectedResp.getEndpoint();
        validate(expectedID, 0, 3, "Frame ID needs to be between 1..3");
        validate(eEndpoint, 0, 3, "Endpoint must be 0..3");
        validate(expectedResp.getCmdLen().getByteVal(), 0, 3, "cmdLen must be 0..3");

        byte[] rxHdr;
        try{
            rxHdr = con.readData(1);
        }catch(Exception e){
            throw new Exception("Read failed, error: " + e);
        }
        FramingHdr hdr;
        try{
            hdr = parseFrame(rxHdr[0]);
        }catch(Exception e){
            throw new Exception("Couldn't parse framing header. Failed with error: " + e);
        }
        if(hdr.getResponseNotOk()){
            con.readData(hdr.getCmdLen().getBytelen());
            throw new Exception("Response status not OK");
        }
        if(hdr.getCmdLen() != expectedResp.getCmdLen()) System.out.println("Expected cmdlen " + expectedResp.getCmdLen() + " , got" + hdr.getCmdLen());

        if(hdr.getID() != expectedID) System.out.println("miss-match ID" + " real id: " + hdr.getID() + " expected id: " + expectedID);

        if(hdr.getEndpoint() != eEndpoint) System.out.println("miss-match endpoint" + " real end: " + hdr.getEndpoint() + " expected end: " + eEndpoint);

        byte[] rx = new byte[1+(expectedResp.getCmdLen().getBytelen())];
        rx[0] = rxHdr[0];
        int eRespCode = expectedResp.getCode();
        try{
            Thread.sleep(10); //Required, otherwise entire readBuffer is not read
            rx = con.readData(rx.length);
        } catch(Exception e){
            throw new Exception("Read failed, error: " + e);
        }
        if(rx[0] != eRespCode){
            System.out.println("Expected cmd code 0x" + eRespCode + ", got 0x" + rx[1]);
            System.out.println("If this happens more than once during app loading, check device app and restart is recommended!");
        }
        return rx;
    }

    /**
     * Helper method for validating that values are as expected/within a certain accepted range.
     */
    private void validate(int value, int min, int max, String errorMessage) throws Exception {
        if (value < min || value > max) throw new Exception(errorMessage);
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