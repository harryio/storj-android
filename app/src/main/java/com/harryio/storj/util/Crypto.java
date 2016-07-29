package com.harryio.storj.util;

import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.util.encoders.Hex;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Random;

public class Crypto {
    /**
     * Sign a byte array with the provided algorithm
     * @param algo algorithm to be used when signing a string
     * @param provider provider for the signing algorithm
     * @param privateKey private to be user for signing the string
     * @param data byte array to be signed
     * @return signature byte array
     */
    public static byte[] signString(String algo, String provider, PrivateKey privateKey, byte[] data) {
        try {
            Signature signature = Signature.getInstance(algo, provider);
            signature.initSign(privateKey);
            signature.update(data);
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
        return sha256Digest(str.getBytes());
    }

    /**
     * Computes SHA-256 digest
     * @return SHA-256 digest
     */
    public static byte[] sha256Digest(byte[] input) {
        SHA256Digest digest = new SHA256Digest();
        digest.update(input, 0, input.length);
        byte[] output = new byte[digest.getDigestSize()];

        digest.doFinal(output, 0);
        return output;
    }

    /**
     * Returns the hex encoded SHA-256 digest of the input
     */
    public static byte[] hexSha256Digest(byte[] input) {
        return Hex.encode(sha256Digest(input));
    }

    /**
     * Computes RIPEMD-160 digest
     * @return  RIPEMD-160 digest
     */
    public static byte[] rmd160Digest(byte[] input) {
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(input, 0, input.length);
        byte[] output = new byte[digest.getDigestSize()];

        digest.doFinal(output, 0);
        return output;
    }

    /**
     * Returns the hex encoded RIPEMD-160 digest of the input
     */
    public static byte[] hexRmd160Digest(byte[] input) {
        return Hex.encode(rmd160Digest(input));
    }

    /**
     * <ol>
     *     <li>Calculates SHA-256 digest of the input and hex encodes it</li>
     *     <li>Calculates RIPEMD-160 digest of the hex from the previous step</li>
     *     <li>Hex encodes the RIPEMD-160 digest calculated in the previous step and returns it</li>
     * </ol>
     */
    public static byte[] hexRmd160Sha256Digest(byte[] input) {
        byte[] sha256 = hexSha256Digest(input);
        return hexRmd160Digest(sha256);
    }

    /**
     * Generate a random hex string
     * @param numChars length of string
     * @return hex string
     */
    public static String randomHexString(int numChars) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(32);

        while (stringBuilder.length() < numChars) {
            stringBuilder.append(Integer.toHexString(random.nextInt()));
        }

        return stringBuilder.toString().substring(0, numChars);
    }
}
