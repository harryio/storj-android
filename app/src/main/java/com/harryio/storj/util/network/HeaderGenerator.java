package com.harryio.storj.util.network;

import android.content.Context;

import com.harryio.storj.database.KeyPairDAO;
import com.harryio.storj.util.ECUtils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class HeaderGenerator {
    private static HeaderGenerator headerGenerator;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String hexEncodedPublicKey;

    private HeaderGenerator(Context context) {
        KeyPairDAO keyPairDAO = KeyPairDAO.getInstance(context);
        publicKey = keyPairDAO.getPublicKey();
        privateKey = keyPairDAO.getPrivateKey();
    }

    public static HeaderGenerator getInstance(Context context) {
        if (headerGenerator == null) {
            headerGenerator = new HeaderGenerator(context);
        }
        return headerGenerator;
    }

    public String getHexEncodedSignature(String method, String endpoint, String params) {
        String toBeSignedString = method + "\n" + endpoint + "\n" + params;
        return ECUtils.getHexEncodedSignature(privateKey, toBeSignedString);
    }

    public String getHexEncodedPublicKey() throws IOException, InvalidKeyException {
        if (hexEncodedPublicKey == null) {
            hexEncodedPublicKey = ECUtils.getHexEncodedPublicKey(publicKey);
        }

        return hexEncodedPublicKey;
    }
}
