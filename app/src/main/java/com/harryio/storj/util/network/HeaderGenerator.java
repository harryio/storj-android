package com.harryio.storj.util.network;

import android.util.Base64;

import com.harryio.storj.database.KeyPairDAO;
import com.harryio.storj.util.Crypto;
import com.harryio.storj.util.ECUtils;

import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HeaderGenerator {
    private static HeaderGenerator headerGenerator;
    private KeyPairDAO keyPairDAO;
    private String hexEncodedPublicKey;

    private HeaderGenerator(KeyPairDAO keyPairDAO) {
        this.keyPairDAO = keyPairDAO;
    }

    public static HeaderGenerator getInstance(KeyPairDAO keyPairDAO) {
        if (headerGenerator == null) {
            headerGenerator = new HeaderGenerator(keyPairDAO);
        }
        return headerGenerator;
    }

    public static String getAuthHeader(String username, String password) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String passwordHash = Hex.toHexString(Crypto.sha256Digest(password));

        String str = username + ":" + passwordHash;
        byte[] data = str.getBytes();
        return "basic " + Base64.encodeToString(data, Base64.NO_WRAP | Base64.URL_SAFE);
    }

    public String getSignatureHeader(String method, String endpoint, String params) {
        String toBeSignedString = method + "\n" + endpoint + "\n" + params;
        return ECUtils.getHexEncodedSignature(keyPairDAO.getPrivateKey(), toBeSignedString);
    }

    public String getPublicKeyHeader() throws IOException, InvalidKeyException {
        if (hexEncodedPublicKey == null) {
            hexEncodedPublicKey = ECUtils.getHexEncodedPublicKey(keyPairDAO.getPublicKey());
        }

        return hexEncodedPublicKey;
    }
}
