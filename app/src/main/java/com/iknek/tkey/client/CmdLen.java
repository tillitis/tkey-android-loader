package com.iknek.tkey.client;

public enum CmdLen {
    CmdLen1(1, 0),
    CmdLen4(4, 1),
    CmdLen32(32, 2),
    CmdLen128(128, 3);

    private final int byteLen;
    private final int byteVal;

    CmdLen(int byteLen, int byteVal) {
        this.byteLen = byteLen;
        this.byteVal = byteVal;
    }

    int getBytelen() {
        return byteLen;
    }

    int getByteVal() {
        return byteVal;
    }
}
