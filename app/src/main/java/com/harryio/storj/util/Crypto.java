package com.harryio.storj.util;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

public class Crypto {
    /**
     * Sign a string with the provided algorithm
     * @param algo algorithm to be used when signing a string
     * @param provider provider for the signing algorithm
     * @param privateKey private key to be used for signing the string
     * @param str string to be signed
     * @return signature byte array
     */
    public static byte[] signString(String algo, String provider, PrivateKey privateKey, String str) {
        try {
            Signature signature = Signature.getInstance(algo, provider);
            signature.initSign(privateKey);
            byte[] strBytes = str.getBytes();
            signature.update(strBytes);
            return signature.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Produce SHA-256 digest of the string
     * @return digest of the string
     */
    public static byte[] sha256Digest(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes());
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }
}
