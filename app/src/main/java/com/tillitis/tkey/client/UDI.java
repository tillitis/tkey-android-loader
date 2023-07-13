/*
 * Copyright (C) 2022, 2023 - Tillitis AB
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.tillitis.tkey.client;

public final class UDI {
    private final int vpr;
    private final int unnamed;
    private final int vendorID;
    private final int productID;
    private final int productRevision;
    private final int serial;
    private final short[] udi;

    public UDI(int vpr, int unnamed, int vendorID, int productID, int productRevision, int serial, short[] udi) {
        this.vpr = vpr;
        this.unnamed = unnamed;
        this.vendorID = vendorID;
        this.productID = productID;
        this.productRevision = productRevision;
        this.serial = serial;
        this.udi = udi;
    }

    public int getVpr() {
        return vpr;
    }

    public int getUnnamed() {
        return unnamed;
    }

    public int getVendorID() {
        return vendorID;
    }

    public int getProductID() {
        return productID;
    }

    public int getProductRevision() {
        return productRevision;
    }

    public int getSerial() {
        return serial;
    }

    public short[] getUdi() {
        return udi;
    }

}

