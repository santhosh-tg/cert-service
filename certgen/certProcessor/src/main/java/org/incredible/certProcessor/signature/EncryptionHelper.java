package org.incredible.certProcessor.signature;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;

/**
 * A simple helper util to demonstrate encryption.
 * In reality, use a list of KeyPairs and also persist them to a database store
 * for re-use.
 */
public class EncryptionHelper {
    private KeyPair key;
    private Cipher cipher;

    public EncryptionHelper(KeyPair key) {
        this.key = key;
    }

    public EncryptionHelper() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
//         https://docs.oracle.com/javase/8/docs/api/javax/crypto/Cipher.html
        // Creates an RSA Cipher object (specifying the algorithm, mode, and padding).
        cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");

        // Initializes the Cipher object.
        cipher.init(Cipher.ENCRYPT_MODE, key.getPublic());
    }


    public byte[] encryptData(byte[] data,
                                     Certificate encryptionCertificate)
            throws BadPaddingException, IllegalBlockSizeException {
        byte[] cipherText = cipher.doFinal(data);
        return cipherText;
    }


    public byte[] decryptData(byte[] cipherText)
            throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException {

        // Initializes the Cipher object.
        cipher.init(Cipher.DECRYPT_MODE, key.getPrivate());

        // Decrypt the ciphertext using the private key
        byte[] newPlainText = cipher.doFinal(cipherText);
        return newPlainText;
    }
}
