/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.tillitis.tkey.client;

public class Tuple {
    private final int intValue;
    private final byte[] byteArray;

    public Tuple(byte[] byteArray, int intValue) {
        this.byteArray = byteArray;
        this.intValue = intValue;
    }

    public byte[] getByteArray(){
        return byteArray;
    }

    public int getIntValue() {
        return intValue;
    }
}
