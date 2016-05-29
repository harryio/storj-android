package com.harryio.storj.util.network;

import com.harryio.storj.database.KeyPairDAO;
import com.harryio.storj.util.ECUtils;

import java.io.IOException;
import java.security.InvalidKeyException;

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
