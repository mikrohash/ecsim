package org.iota.ecsim.utils;

public class Trytes {

    public static final String NULL_HASH = "999999999999999999999999999999999999999999999999999999999999999999999999999999999";
    public static final String TRYTES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ9";

    private static String randomTrytesOfLength(int length) {
        char[] hash = new char[length];
        for(int i = 0; i < length; i++)
            hash[i] = TRYTES.charAt((int)(Math.random()*TRYTES.length()));
        return new String(hash);
    }

    public static String randomHash() {
        return randomTrytesOfLength(81);
    }
}
