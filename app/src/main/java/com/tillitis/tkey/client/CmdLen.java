/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.tillitis.tkey.client;

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

    public int getBytelen() {
        return byteLen;
    }

    int getByteVal() {
        return byteVal;
    }
}
