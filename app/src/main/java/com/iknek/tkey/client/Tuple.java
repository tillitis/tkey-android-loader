package com.iknek.tkey.client;

public class Tuple {
    private final int intValue;
    private final int[] intArray;

    public Tuple(int[] byteArray, int intValue) {
        this.intArray = byteArray;
        this.intValue = intValue;
    }

    public int[] getIntArray() {
        return intArray;
    }
    public int getIntValue() {
        return intValue;
    }
}
