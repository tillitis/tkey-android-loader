package com.iknek.tkey.client;

import java.util.Objects;

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

    public int vpr() {
        return vpr;
    }

    public int unnamed() {
        return unnamed;
    }

    public int vendorID() {
        return vendorID;
    }

    public int productID() {
        return productID;
    }

    public int productRevision() {
        return productRevision;
    }

    public int serial() {
        return serial;
    }

    public short[] udi() {
        return udi;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UDI) obj;
        return this.vpr == that.vpr &&
                this.unnamed == that.unnamed &&
                this.vendorID == that.vendorID &&
                this.productID == that.productID &&
                this.productRevision == that.productRevision &&
                this.serial == that.serial &&
                Objects.equals(this.udi, that.udi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vpr, unnamed, vendorID, productID, productRevision, serial, udi);
    }

    @Override
    public String toString() {
        return "UDI[" +
                "vpr=" + vpr + ", " +
                "unnamed=" + unnamed + ", " +
                "vendorID=" + vendorID + ", " +
                "productID=" + productID + ", " +
                "productRevision=" + productRevision + ", " +
                "serial=" + serial + ", " +
                "udi=" + udi + ']';
    }

}

