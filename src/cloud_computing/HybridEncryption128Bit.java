/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cloud_computing;

import java.io.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.CipherOutputStream;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class HybridEncryption128Bit {
    public static void main(String[] args) throws Exception {
        // Add Bouncy Castle as a Security Provider
        
Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // Paths for saving encrypted outputs
        String file = "phr1.txt"; // File to be encrypted
        String encryptedFilePath = "encrypted_file.bin";
        String encryptedKeyPath = "encrypted_aes_key.bin";
        String signaturePath = "signature.bin";

        // Step 1: Generate AES Key (128-bit)
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // AES key size set to 128 bits
        SecretKey aesKey = keyGen.generateKey();

        // Step 2: Encrypt the file with AES
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
        try (FileInputStream fis = new FileInputStream(file);
             FileOutputStream fos = new FileOutputStream(encryptedFilePath);
             CipherOutputStream cos = new CipherOutputStream(fos, aesCipher)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        }

        // Step 3: Generate ECC Key Pair
        KeyPairGenerator ecKeyPairGen = KeyPairGenerator.getInstance("EC", "BC");
        ecKeyPairGen.initialize(new ECGenParameterSpec("secp256r1")); // 256-bit ECC curve (128-bit security)
        KeyPair ecKeyPair = ecKeyPairGen.generateKeyPair();
        PublicKey ecPublicKey = ecKeyPair.getPublic();
        PrivateKey ecPrivateKey = ecKeyPair.getPrivate();

        // Step 4: Encrypt the AES Key using ECC Public Key
        Cipher ecCipher = Cipher.getInstance("ECIES", "BC");
        ecCipher.init(Cipher.ENCRYPT_MODE, ecPublicKey);
        byte[] encryptedAesKey = ecCipher.doFinal(aesKey.getEncoded());

        // Save the encrypted AES key
        try (FileOutputStream keyOut = new FileOutputStream(encryptedKeyPath)) {
            keyOut.write(encryptedAesKey);
        }

        // Step 5: Sign the Encrypted File using ECC Private Key
        Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
        signature.initSign(ecPrivateKey);
        try (FileInputStream efis = new FileInputStream(encryptedFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = efis.read(buffer)) != -1) {
                signature.update(buffer, 0, bytesRead);
            }
        }
        byte[] digitalSignature = signature.sign();

        // Save the signature
        try (FileOutputStream sigOut = new FileOutputStream(signaturePath)) {
            sigOut.write(digitalSignature);
        }

        System.out.println("File encrypted and signed successfully using Hybrid AES and ECC.");
    }
}
