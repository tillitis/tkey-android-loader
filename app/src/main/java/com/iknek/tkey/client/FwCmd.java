package com.iknek.tkey.client;

public class FwCmd{
    private final int code;
    private final String name;
    private final CmdLen cmdLen;
    final byte endpoint;

    public FwCmd(int code, String name, CmdLen cmdLen) {
        this.code = code;
        this.name = name;
        this.cmdLen = cmdLen;
        this.endpoint = 2;
    }

    public FwCmd(int code, String name, CmdLen cmdLen, byte endpoint) {
        this.code = code;
        this.name = name;
        this.cmdLen = cmdLen;
        this.endpoint = endpoint;
    }

    public int getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public byte getEndpoint(){
        return endpoint;
    }

    public CmdLen getCmdLen() {
        return cmdLen;
    }
}