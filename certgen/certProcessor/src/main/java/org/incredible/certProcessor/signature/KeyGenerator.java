package org.incredible.certProcessor.signature;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class KeyGenerator {
    KeyPairGenerator keyGen;

    /**
     * See https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html
     * @param algorithm - example, "RSA".
     * @param keySize - 1024 or 2048
     * @throws NoSuchAlgorithmException
     */
    public KeyGenerator(String algorithm, int keySize) throws NoSuchAlgorithmException {
        keyGen = KeyPairGenerator.getInstance(algorithm);
        keyGen.initialize(keySize);
    }

    /**
     * Gets a keypair
     * @return
     */
    public KeyPair get() {
        return keyGen.generateKeyPair();
    }
}
