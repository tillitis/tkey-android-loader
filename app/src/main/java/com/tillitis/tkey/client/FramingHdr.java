/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.tillitis.tkey.client;

public class FramingHdr {
    private final int ID;
    private final int endpoint;
    private final CmdLen cmdLen;
    private boolean ResponseNotOk;

    public FramingHdr(int ID, int endpoint, CmdLen cmdLen){
        this.cmdLen = cmdLen;
        this.ID = ID;
        this.endpoint = endpoint;
        this.ResponseNotOk = false;
    }

    public int getID() {
        return ID;
    }

    public int getEndpoint() {
        return endpoint;
    }

    public CmdLen getCmdLen() {
        return cmdLen;
    }

    public boolean getResponseNotOk() {
        return ResponseNotOk;
    }

    public void setResponseNotOk(boolean response){
        ResponseNotOk = response;
    }
}
