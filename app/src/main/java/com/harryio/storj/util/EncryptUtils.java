package com.harryio.storj.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtils {
    public static void encrypt(File input, File output, String key) throws
            IOException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException {
        FileInputStream fileInputStream = new FileInputStream(input);
        FileOutputStream fileOutputStream = new FileOutputStream(output);

        byte[] keyBytes = getKeyBytes(key.getBytes("UTF-8"));
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, cipher);
        int read;
        byte[] data = new byte[1024];
        while ((read = fileInputStream.read(data)) != -1) {
            cipherOutputStream.write(data, 0, read);
        }

        cipherOutputStream.flush();
        cipherOutputStream.close();
        fileInputStream.close();
    }

    private static byte[] getKeyBytes(final byte[] key) {
        byte[] keyBytes = new byte[16];
        System.arraycopy(key, 0, keyBytes, 0, Math.min(key.length, keyBytes.length));
        return keyBytes;
    }
}
