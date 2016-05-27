package com.harryio.storj.util;

import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class ECUtils {
    private static final ECDomainParameters ecParams;

    static {
        X9ECParameters params = SECNamedCurves.getByName("secp256k1");
        ecParams = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
    }

    private ECUtils() {
    }

    /**
     * Creates a new pair of {@link PublicKey} and {@link PrivateKey} ECDSA keys
     *
     * @throws NoSuchAlgorithmException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     */
    public static KeyPair getKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "SC");
        ECGenParameterSpec spec = new ECGenParameterSpec("secp256k1");
        keyPairGenerator.initialize(spec, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Get {@link PublicKey} from byte array
     */
    public static PublicKey getPublicKey(byte[] encodedPublicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "SC");
            return keyFactory.generatePublic(new X509EncodedKeySpec(encodedPublicKey));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get {@link PrivateKey} from byte array
     */
    public static PrivateKey getPrivateKey(byte[] encodedPrivateKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "SC");
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedPrivateKey));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get hex encoded representation of {@link PublicKey}
     * @throws IOException
     */
    public static String getHexEncodedPublicKey(PublicKey publicKey) throws IOException, InvalidKeyException {
        ECPublicKeyParameters ecPublicKeyParameters
                = (ECPublicKeyParameters) ECUtil.generatePublicKeyParameter(publicKey);
        byte[] encoded = ecPublicKeyParameters.getQ().getEncoded(false);
        return Hex.toHexString(encoded);
    }

    /**
     * Sign string using ECDSA algo
     * @return signature
     */
    public static byte[] sign(PrivateKey privateKey, String string) {
        return Crypto.signString("SHA256withECDSA", "SC", privateKey, string);
    }

    /**
     * Retruns the hex encoded string of the signature
     */
    public static String getHexEncodedSignature(PrivateKey privateKey, String string) {
        byte[] signature = sign(privateKey, string);
        return Hex.toHexString(signature);
    }
}
