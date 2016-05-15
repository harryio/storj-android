package com.harryio.storj.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA {
    public static String hash256(String str) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(str.getBytes());
        return bytesToHex(messageDigest.digest());
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }
}
