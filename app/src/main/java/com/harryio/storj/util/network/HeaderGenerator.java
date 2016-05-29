package com.harryio.storj.util.network;

import android.content.Context;

import com.harryio.storj.database.KeyPairDAO;
import com.harryio.storj.util.ECUtils;

import java.io.IOException;
import java.security.InvalidKeyException;

public class HeaderGenerator {
    private static HeaderGenerator headerGenerator;
    private KeyPairDAO keyPairDAO;
    private String hexEncodedPublicKey;

    private HeaderGenerator(Context context) {
        keyPairDAO = KeyPairDAO.getInstance(context);
    }

    public static HeaderGenerator getInstance(Context context) {
        if (headerGenerator == null) {
            headerGenerator = new HeaderGenerator(context);
        }
        return headerGenerator;
    }

    public String getHexEncodedSignature(String method, String endpoint, String params) {
        String toBeSignedString = method + "\n" + endpoint + "\n" + params;
        return ECUtils.getHexEncodedSignature(keyPairDAO.getPrivateKey(), toBeSignedString);
    }

    public String getHexEncodedPublicKey() throws IOException, InvalidKeyException {
        if (hexEncodedPublicKey == null) {
            hexEncodedPublicKey = ECUtils.getHexEncodedPublicKey(keyPairDAO.getPublicKey());
        }

        return hexEncodedPublicKey;
    }
}
