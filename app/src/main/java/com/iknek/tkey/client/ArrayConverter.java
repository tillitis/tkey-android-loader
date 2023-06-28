package com.iknek.tkey.client;

public class ArrayConverter {

    static short[] bytesToUnsignedBytes(byte[] bytes) {
        short[] unsignedBytes = new short[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            unsignedBytes[i] = (short) (bytes[i] & 0xFF);
        }
        return unsignedBytes;
    }

    static byte[] intArrayToByteArray(int[] intArray) {
        byte[] arr = new byte[intArray.length];
        for(int i = 0; i<intArray.length; i++){
            arr[i] = (byte) intArray[i];
        }
        return arr;
    }

    static int[] byteArrayToIntArray(byte[] byteArray) {
        int[] arr = new int[byteArray.length];
        for(int i = 0; i<byteArray.length; i++){
            arr[i] = byteArray[i];
        }
        return arr;
    }
}
