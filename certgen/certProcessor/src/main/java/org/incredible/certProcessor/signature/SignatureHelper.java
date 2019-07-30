package org.incredible.certProcessor.signature;


import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;

public class SignatureHelper {
    private Signature signature;
    private KeyPair keyPair;
    /**
     * For a list of RSA-based Signature Algorithms, with MD2, MD5 or SHA-1,
     * refer to https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Algorithm
     * @param hashAlgorithm, valid values are MD2withRSA, MD5withRSA and SHA1withRSA
     * @throws NoSuchAlgorithmException
     */
    public SignatureHelper(String hashAlgorithm, KeyPair keyPair) throws NoSuchAlgorithmException {
        signature = Signature.getInstance(hashAlgorithm);
        this.keyPair = keyPair;
    }

    public String sign(byte[] data) throws SignatureException, InvalidKeyException, UnsupportedEncodingException {
        // After doing a sign, the keys are going to be reset. So do this everytime.
        signature.initSign(keyPair.getPrivate());
        signature.update(data);
        byte[] signed = signature.sign();
        return Base64.encode(signed);
    }

    public boolean verify(byte[] data, String signatureData) throws InvalidKeyException, SignatureException {
        signature.initVerify(keyPair.getPublic());
        signature.update(data);
        byte[] decodedSignature = new Base64().decode(signatureData);
        return signature.verify(decodedSignature);
    }
}
