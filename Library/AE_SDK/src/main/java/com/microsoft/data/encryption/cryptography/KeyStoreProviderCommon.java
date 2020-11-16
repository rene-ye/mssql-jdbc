package com.microsoft.data.encryption.cryptography;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/**
 * 
 * This class holds information about the certificate
 *
 */
class CertificateDetails {
    X509Certificate certificate;
    Key privateKey;

    CertificateDetails(X509Certificate certificate, Key privateKey) {
        this.certificate = certificate;
        this.privateKey = privateKey;
    }
}


class KeyStoreProviderCommon {

    static final String rsaEncryptionAlgorithmWithOAEP = "RSA_OAEP";
    static byte[] version = new byte[] {0x01};

    static void validateEncryptionAlgorithm(String encryptionAlgorithm, boolean isEncrypt) throws AAPSDKException {
        if (null == encryptionAlgorithm) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_NullKeyEncryptionAlgorithm"));
        }

        if (!rsaEncryptionAlgorithmWithOAEP.equalsIgnoreCase(encryptionAlgorithm.trim())) {
            MessageFormat form = new MessageFormat(AAPSDKResource.getResource("R_InvalidKeyEncryptionAlgorithm"));
            Object[] msgArgs = {encryptionAlgorithm, rsaEncryptionAlgorithmWithOAEP};
            throw new AAPSDKException(form.format(msgArgs));
        }
    }

    static void validateNonEmptyMasterKeyPath(String masterKeyPath) throws AAPSDKException {
        if (null == masterKeyPath || masterKeyPath.trim().length() == 0) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_InvalidMasterKeyDetails"));
        }
    }

    static byte[] decryptColumnEncryptionKey(String masterKeyPath, String encryptionAlgorithm,
            byte[] encryptedColumnEncryptionKey, CertificateDetails certificateDetails) throws AAPSDKException {
        if (null == encryptedColumnEncryptionKey) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_EncryptedCEKNull"));
        } else if (0 == encryptedColumnEncryptionKey.length) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_EmptyEncryptedCEK"));
        }

        validateEncryptionAlgorithm(encryptionAlgorithm, false);

        int currentIndex = version.length;
        int keyPathLength = convertTwoBytesToShort(encryptedColumnEncryptionKey, currentIndex);
        // We just read 2 bytes
        currentIndex += 2;

        // Get ciphertext length
        int cipherTextLength = convertTwoBytesToShort(encryptedColumnEncryptionKey, currentIndex);
        currentIndex += 2;

        currentIndex += keyPathLength;

        int signatureLength = encryptedColumnEncryptionKey.length - currentIndex - cipherTextLength;

        // Get ciphertext
        byte[] cipherText = new byte[cipherTextLength];
        System.arraycopy(encryptedColumnEncryptionKey, currentIndex, cipherText, 0, cipherTextLength);
        currentIndex += cipherTextLength;

        byte[] signature = new byte[signatureLength];
        System.arraycopy(encryptedColumnEncryptionKey, currentIndex, signature, 0, signatureLength);

        byte[] hash = new byte[encryptedColumnEncryptionKey.length - signature.length];

        System.arraycopy(encryptedColumnEncryptionKey, 0, hash, 0,
                encryptedColumnEncryptionKey.length - signature.length);

        if (!verifyRSASignature(hash, signature, certificateDetails.certificate, masterKeyPath)) {
            MessageFormat form = new MessageFormat(AAPSDKResource.getResource("R_CEKSignatureNotMatchCMK"));
            Object[] msgArgs = {masterKeyPath};
            throw new AAPSDKException(form.format(msgArgs));
        }

        byte[] plainCEK = decryptRSAOAEP(cipherText, certificateDetails);

        return plainCEK;
    }

    private static byte[] decryptRSAOAEP(byte[] cipherText,
            CertificateDetails certificateDetails) throws AAPSDKException {
        byte[] plainCEK = null;
        try {
            Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            rsa.init(Cipher.DECRYPT_MODE, certificateDetails.privateKey);
            rsa.update(cipherText);
            plainCEK = rsa.doFinal();
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                | BadPaddingException e) {
            MessageFormat form = new MessageFormat(AAPSDKResource.getResource("R_ByteToShortConversion"));
            Object[] msgArgs = {e.getMessage()};
            throw new AAPSDKException(form.format(msgArgs));
        }

        return plainCEK;

    }

    static boolean verifyRSASignature(byte[] hash, byte[] signature, X509Certificate certificate,
            String masterKeyPath) throws AAPSDKException {
        Signature signVerify;
        boolean verificationSuccess = false;
        try {
            signVerify = Signature.getInstance("SHA256withRSA");
            signVerify.initVerify(certificate.getPublicKey());
            signVerify.update(hash);
            verificationSuccess = signVerify.verify(signature);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            MessageFormat form = new MessageFormat(AAPSDKResource.getResource("R_CEKSignatureNotMatchCMK"));
            Object[] msgArgs = {masterKeyPath};
            throw new AAPSDKException(form.format(msgArgs));
        }

        return verificationSuccess;

    }

    private static short convertTwoBytesToShort(byte[] input, int index) throws AAPSDKException {

        short shortVal;
        if (index + 1 >= input.length) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_CEKDecryptionFailed"));
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(input[index]);
        byteBuffer.put(input[index + 1]);
        shortVal = byteBuffer.getShort(0);
        return shortVal;

    }
}
