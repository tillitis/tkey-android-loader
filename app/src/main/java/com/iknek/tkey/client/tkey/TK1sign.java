package com.iknek.tkey.client.tkey;

import com.iknek.tkey.client.CmdLen;
import com.iknek.tkey.client.FwCmd;
import com.iknek.tkey.client.TkeyClient;

public class TK1sign {
    private static final FwCmd cmdGetPubkey       = new FwCmd(0x01, "cmdGetPubkey", CmdLen.CmdLen1, (byte) 3);
    private static final FwCmd rspGetPubkey       = new FwCmd(0x02, "rspGetPubkey", CmdLen.CmdLen128, (byte) 3);
    private static final FwCmd cmdSetSize         = new FwCmd(0x03, "cmdSetSize", CmdLen.CmdLen32, (byte) 3);
    private static final FwCmd rspSetSize         = new FwCmd(0x04, "rspSetSize", CmdLen.CmdLen4, (byte) 3);
    private static final FwCmd cmdSignData        = new FwCmd(0x05, "cmdSignData", CmdLen.CmdLen128, (byte) 3);
    private static final FwCmd rspSignData        = new FwCmd(0x06, "rspSignData", CmdLen.CmdLen4, (byte) 3);
    private static final FwCmd cmdGetSig          = new FwCmd(0x07, "cmdGetSig", CmdLen.CmdLen1, (byte) 3);
    private static final FwCmd rspGetSig          = new FwCmd(0x08, "rspGetSig", CmdLen.CmdLen128, (byte) 3);
    private static final FwCmd cmdGetNameVersion  = new FwCmd(0x09, "cmdGetNameVersion", CmdLen.CmdLen1, (byte) 3);
    private static final FwCmd rspGetNameVersion  = new FwCmd(0x0a, "rspGetNameVersion", CmdLen.CmdLen32, (byte) 3);
    private static final FwCmd cmdGetFirmwareHash = new FwCmd(0x0b, "cmdGetFirmwareHash", CmdLen.CmdLen32, (byte) 3);
    private static final FwCmd rspGetFirmwareHash = new FwCmd(0x0c, "rspGetFirmwareHash", CmdLen.CmdLen128, (byte) 3);

    private static final int maxSignSize = 4096;
    private TkeyClient tk1;

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
}
