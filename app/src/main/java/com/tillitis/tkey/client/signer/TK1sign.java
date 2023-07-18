/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.tillitis.tkey.client.signer;

import static com.tillitis.tkey.client.CmdLen.*;
import com.tillitis.tkey.client.FwCmd;
import com.tillitis.tkey.client.TkeyClient;
import com.tillitis.tkey.client.proto;

import java.util.Arrays;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPublicKey;

public class TK1sign {
    private static final FwCmd cmdGetPubkey       = new FwCmd(0x01, "cmdGetPubkey", CmdLen1, (byte) 3);
    private static final FwCmd rspGetPubkey       = new FwCmd(0x02, "rspGetPubkey", CmdLen128, (byte) 3);
    private static final FwCmd cmdSetSize         = new FwCmd(0x03, "cmdSetSize", CmdLen32, (byte) 3);
    private static final FwCmd rspSetSize         = new FwCmd(0x04, "rspSetSize", CmdLen4, (byte) 3);
    private static final FwCmd cmdSignData        = new FwCmd(0x05, "cmdSignData", CmdLen128, (byte) 3);
    private static final FwCmd rspSignData        = new FwCmd(0x06, "rspSignData", CmdLen4, (byte) 3);
    private static final FwCmd cmdGetSig          = new FwCmd(0x07, "cmdGetSig", CmdLen1, (byte) 3);
    private static final FwCmd rspGetSig          = new FwCmd(0x08, "rspGetSig", CmdLen128, (byte) 3);
    private static final FwCmd cmdGetNameVersion  = new FwCmd(0x09, "cmdGetNameVersion", CmdLen1, (byte) 3);
    private static final FwCmd rspGetNameVersion  = new FwCmd(0x0a, "rspGetNameVersion", CmdLen32, (byte) 3);
    private static final FwCmd cmdGetFirmwareHash = new FwCmd(0x0b, "cmdGetFirmwareHash", CmdLen32, (byte) 3);
    private static final FwCmd rspGetFirmwareHash = new FwCmd(0x0c, "rspGetFirmwareHash", CmdLen128, (byte) 3);

    private static final int maxSignSize = 4096;
    private TkeyClient tk1;

    private final byte statusOK = 0x00;

    public TK1sign(TkeyClient tkeyClient){
        tk1 = tkeyClient;
    }

    public String getAppNameVersion() throws Exception {
        tk1.clearIO();
        byte[] tx = tk1.newFrameBuf(cmdGetNameVersion,2);
        tk1.write(tx);
        Thread.sleep(500);
        byte[] rx = tk1.readFrame(rspGetNameVersion,2);
        return TkeyClient.unpackName(rx);
    }

    public byte[] getPubKey() throws Exception {

        byte[] tx = tk1.newFrameBuf(cmdGetPubkey,2);
        tk1.dump("S", tx);
        tk1.write(tx);
        return tk1.readFrame(rspGetPubkey,2);
    }

    public byte[] getSig() throws Exception {
        byte[] tx = tk1.newFrameBuf(cmdGetSig,2);

        tk1.write(tx);
        Thread.sleep(500);
        byte[] rx = tk1.readFrame(rspGetSig,2);

        if(rx[2] != statusOK){
            System.out.println("Status not ok");
        }
        return Arrays.copyOfRange(rx,3,rx.length);
    }

    public byte[] sign(byte[] in) throws Exception {
        setSize(in.length);
        int offset = 0;
        for(int nsent = 0; offset < in.length; offset+= nsent){
            nsent = signLoad(Arrays.copyOfRange(in, offset, in.length));
        }
        if(offset > in.length) throw new Exception("Transmitted more than expected");

        return getSig();
    }

    public void setSize(int size) throws Exception {
        byte[] tx = tk1.newFrameBuf(cmdSetSize,2);
        tx[2] = (byte) size;
        tx[3] = (byte) (size >> 8);
        tx[4] = (byte) (size >> 16);
        tx[5] = (byte) (size >> 24);
        tk1.write(tx);

        Thread.sleep(500);

        byte[] rx = tk1.readFrame(rspSetSize,2);
        if(rx[2] != statusOK){
            System.out.println("Status not ok");
        }
    }

    private int signLoad(byte[] content) throws Exception {
        byte[] tx = tk1.newFrameBuf(cmdSignData,2);

        byte[] payload = new byte[cmdSignData.getCmdLen().getBytelen()-1];
        int copied = Math.min(payload.length, content.length);
        System.arraycopy(content, 0, payload, 0, copied);

        if (copied < payload.length) {
            byte[] padding = new byte[payload.length - copied];
            System.arraycopy(padding, 0, payload, copied, padding.length-1);
        }
        System.arraycopy(payload, 0, tx, 2, payload.length);

        tk1.write(tx);

        byte[] rx;
        try {
            Thread.sleep(10);
            rx = tk1.readFrame(rspSignData, 2);
        }catch (Exception e){
            throw new Exception(e);
        }
        if(rx[2] != statusOK){
            System.out.println("Status not ok");
        }
        return copied;
    }
}
