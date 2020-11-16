package com.microsoft.data.encryption.cryptography;

import static java.nio.charset.StandardCharsets.UTF_16LE;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/**
 * 
 * Provides the implementation of the key store provider for Java Key Store. This class enables using certificates
 * stored in the Java keystore as column master keys.
 *
 */
public class EncryptionJavaKeyStoreProvider extends EncryptionKeyStoreProvider {

    String providerName = "MSSQL_JAVA_KEYSTORE";
    String keyStorePath = null;
    char[] keyStorePwd = null;
    static final private java.util.logging.Logger javaKeyStoreLogger = java.util.logging.Logger
            .getLogger("com.microsoft.sqlserver.jdbc.SQLServerColumnEncryptionJavaKeyStoreProvider");

    /**
     * Constructs a EncryptionJavaKeyStoreProvider for the Java Key Store.
     * 
     * @param keyStoreLocation
     *        specifies the location of the keystore
     * @param keyStoreSecret
     *        specifies the secret used for keystore
     * @throws SQLServerException
     *         when an error occurs
     */
    public EncryptionJavaKeyStoreProvider(String keyStoreLocation, char[] keyStoreSecret) throws AAPSDKException {
        javaKeyStoreLogger.entering(EncryptionJavaKeyStoreProvider.class.getName(),
                "SQLServerColumnEncryptionJavaKeyStoreProvider");

        if ((null == keyStoreLocation) || (0 == keyStoreLocation.length())) {
            MessageFormat form = new MessageFormat(AAPSDKException.getErrString("R_InvalidKeyStoreLocation"));
            Object[] msgArgs = {keyStoreLocation};
            throw new AAPSDKException(form.format(msgArgs));
        }

        this.keyStorePath = keyStoreLocation;

        if (javaKeyStoreLogger.isLoggable(java.util.logging.Level.FINE)) {
            javaKeyStoreLogger.fine("Path of key store provider is set.");
        }

        // Password can be null or empty, PKCS12 type allows that.
        if (null == keyStoreSecret) {
            keyStoreSecret = "".toCharArray();
        }

        this.keyStorePwd = new char[keyStoreSecret.length];
        System.arraycopy(keyStoreSecret, 0, this.keyStorePwd, 0, keyStoreSecret.length);

        if (javaKeyStoreLogger.isLoggable(java.util.logging.Level.FINE)) {
            javaKeyStoreLogger.fine("Password for key store provider is set.");
        }

        javaKeyStoreLogger.exiting(EncryptionJavaKeyStoreProvider.class.getName(),
                "SQLServerColumnEncryptionJavaKeyStoreProvider");
    }

    public void setName(String name) {
        this.providerName = name;
    }

    public String getName() {
        return this.providerName;
    }

    @Override
    public byte[] decryptEncryptionKey(String masterKeyPath, String encryptionAlgorithm,
            byte[] encryptedColumnEncryptionKey) throws AAPSDKException {
        javaKeyStoreLogger.entering(EncryptionJavaKeyStoreProvider.class.getName(), "decryptColumnEncryptionKey",
                "Decrypting Column Encryption Key.");

        KeyStoreProviderCommon.validateNonEmptyMasterKeyPath(masterKeyPath);
        CertificateDetails certificateDetails = getCertificateDetails(masterKeyPath);
        byte[] plainCEK = KeyStoreProviderCommon.decryptColumnEncryptionKey(masterKeyPath, encryptionAlgorithm,
                encryptedColumnEncryptionKey, certificateDetails);

        javaKeyStoreLogger.exiting(EncryptionJavaKeyStoreProvider.class.getName(), "decryptColumnEncryptionKey",
                "Finished decrypting Column Encryption Key.");
        return plainCEK;
    }

    @Override
    public byte[] encryptEncryptionKey(String masterKeyPath, String encryptionAlgorithm,
            byte[] columnEncryptionKey) throws AAPSDKException {
        javaKeyStoreLogger.entering(EncryptionJavaKeyStoreProvider.class.getName(), "encryptColumnEncryptionKey",
                "Encrypting Column Encryption Key.");

        byte[] version = KeyStoreProviderCommon.version;
        KeyStoreProviderCommon.validateNonEmptyMasterKeyPath(masterKeyPath);

        if (null == columnEncryptionKey) {
            throw new IllegalArgumentException(AAPSDKResource.getResource("R_NullColumnEncryptionKey"));
        } else if (0 == columnEncryptionKey.length) {
            throw new IllegalArgumentException(AAPSDKResource.getResource("R_EmptyColumnEncryptionKey"));
        }

        KeyStoreProviderCommon.validateEncryptionAlgorithm(encryptionAlgorithm, true);

        CertificateDetails certificateDetails = getCertificateDetails(masterKeyPath);
        byte[] cipherText = encryptRSAOAEP(columnEncryptionKey, certificateDetails);
        byte[] cipherTextLength = getLittleEndianBytesFromShort((short) cipherText.length);
        byte[] masterKeyPathBytes = masterKeyPath.toLowerCase().getBytes(UTF_16LE);

        byte[] keyPathLength = getLittleEndianBytesFromShort((short) masterKeyPathBytes.length);

        byte[] dataToSign = new byte[version.length + keyPathLength.length + cipherTextLength.length
                + masterKeyPathBytes.length + cipherText.length];
        int destinationPosition = version.length;
        System.arraycopy(version, 0, dataToSign, 0, version.length);

        System.arraycopy(keyPathLength, 0, dataToSign, destinationPosition, keyPathLength.length);
        destinationPosition += keyPathLength.length;

        System.arraycopy(cipherTextLength, 0, dataToSign, destinationPosition, cipherTextLength.length);
        destinationPosition += cipherTextLength.length;

        System.arraycopy(masterKeyPathBytes, 0, dataToSign, destinationPosition, masterKeyPathBytes.length);
        destinationPosition += masterKeyPathBytes.length;

        System.arraycopy(cipherText, 0, dataToSign, destinationPosition, cipherText.length);
        byte[] signedHash = rsaSignHashedData(dataToSign, certificateDetails);

        int encryptedColumnEncryptionKeyLength = version.length + cipherTextLength.length + keyPathLength.length
                + cipherText.length + masterKeyPathBytes.length + signedHash.length;
        byte[] encryptedColumnEncryptionKey = new byte[encryptedColumnEncryptionKeyLength];

        int currentIndex = 0;
        System.arraycopy(version, 0, encryptedColumnEncryptionKey, currentIndex, version.length);
        currentIndex += version.length;

        System.arraycopy(keyPathLength, 0, encryptedColumnEncryptionKey, currentIndex, keyPathLength.length);
        currentIndex += keyPathLength.length;

        System.arraycopy(cipherTextLength, 0, encryptedColumnEncryptionKey, currentIndex, cipherTextLength.length);
        currentIndex += cipherTextLength.length;

        System.arraycopy(masterKeyPathBytes, 0, encryptedColumnEncryptionKey, currentIndex, masterKeyPathBytes.length);
        currentIndex += masterKeyPathBytes.length;

        System.arraycopy(cipherText, 0, encryptedColumnEncryptionKey, currentIndex, cipherText.length);
        currentIndex += cipherText.length;

        System.arraycopy(signedHash, 0, encryptedColumnEncryptionKey, currentIndex, signedHash.length);

        javaKeyStoreLogger.exiting(EncryptionJavaKeyStoreProvider.class.getName(), "encryptColumnEncryptionKey",
                "Finished encrypting Column Encryption Key.");
        return encryptedColumnEncryptionKey;

    }

    @Override
    public byte[] signMasterKeyMetadata(String masterKeyPath, boolean allowEnclaveComputations) throws AAPSDKException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean verifyMasterKeyMetadata(String masterKeyPath, boolean allowEnclaveComputations,
            byte[] signature) throws AAPSDKException {
        if (!allowEnclaveComputations)
            return false;

        KeyStoreProviderCommon.validateNonEmptyMasterKeyPath(masterKeyPath);
        CertificateDetails certificateDetails = getCertificateDetails(masterKeyPath);
        if (null == certificateDetails) {
            return false;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(providerName.toLowerCase().getBytes(java.nio.charset.StandardCharsets.UTF_16LE));
            md.update(masterKeyPath.toLowerCase().getBytes(java.nio.charset.StandardCharsets.UTF_16LE));
            md.update("true".getBytes(java.nio.charset.StandardCharsets.UTF_16LE));
            return rsaVerifySignature(md.digest(), signature, certificateDetails);
        } catch (NoSuchAlgorithmException e) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_InvalidAlgorithm"));
        }
    }

    @Override
    public byte[] unwrapKey(String encryptionKeyId, KeyEncryptionKeyAlgorithm algorithm, byte[] encryptedKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] wrapKey(String encryptionKeyId, KeyEncryptionKeyAlgorithm algorithm, byte[] key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] sign(String encryptionKeyId, boolean allowEnclaveComputations) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean verify(String encryptionKeyId, boolean allowEnclaveComputations, byte[] signature) {
        // TODO Auto-generated method stub
        return false;
    }

    private CertificateDetails getCertificateDetails(String masterKeyPath) throws AAPSDKException {
        FileInputStream fis = null;
        KeyStore keyStore = null;
        CertificateDetails certificateDetails = null;

        try {
            if (null == masterKeyPath || 0 == masterKeyPath.length()) {
                throw new AAPSDKException(AAPSDKResource.getResource("R_InvalidMasterKey"));
            }

            try {
                // Try to load JKS first, if fails try PKCS12
                keyStore = KeyStore.getInstance("JKS");
                fis = new FileInputStream(keyStorePath);
                keyStore.load(fis, keyStorePwd);
            } catch (IOException e) {
                if (null != fis)
                    fis.close();

                // Loading as JKS failed, try to load as PKCS12
                keyStore = KeyStore.getInstance("PKCS12");
                fis = new FileInputStream(keyStorePath);
                keyStore.load(fis, keyStorePwd);
            }

            certificateDetails = getCertificateDetailsByAlias(keyStore, masterKeyPath);
        } catch (FileNotFoundException fileNotFound) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_InvalidKeyStorePath"));
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            MessageFormat form = new MessageFormat(AAPSDKResource.getResource("R_InvalidFileFormat"));
            Object[] msgArgs = {keyStorePath};
            throw new AAPSDKException(form.format(msgArgs));
        } finally {
            try {
                if (null != fis)
                    fis.close();
            }
              // Ignore the exception as we are cleaning up.
            catch (IOException e) {}
        }

        return certificateDetails;
    }

    private CertificateDetails getCertificateDetailsByAlias(KeyStore keyStore, String alias) throws AAPSDKException {
        try {
            X509Certificate publicCertificate = (X509Certificate) keyStore.getCertificate(alias);
            Key keyPrivate = keyStore.getKey(alias, keyStorePwd);
            if (null == publicCertificate) {
                // Certificate not found. Throw an exception.
                MessageFormat form = new MessageFormat(AAPSDKResource.getResource("R_CertificateNotFoundForAlias"));
                Object[] msgArgs = {alias, "MSSQL_JAVA_KEYSTORE"};
                throw new AAPSDKException(form.format(msgArgs));
            }

            // found certificate but corresponding private key not found, throw exception
            if (null == keyPrivate) {
                throw new UnrecoverableKeyException();
            }

            return new CertificateDetails(publicCertificate, keyPrivate);
        } catch (UnrecoverableKeyException e) {
            MessageFormat form = new MessageFormat(AAPSDKResource.getResource("R_UnrecoverableKeyAE"));
            Object[] msgArgs = {alias};
            throw new AAPSDKException(form.format(msgArgs));
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            MessageFormat form = new MessageFormat(AAPSDKResource.getResource("R_CertificateError"));
            Object[] msgArgs = {alias, providerName};
            throw new AAPSDKException(form.format(msgArgs));
        }
    }

    /**
     * Encrypt plainText with the certificate provided.
     * 
     * @param plainText
     *        plain CEK to be encrypted
     * @param certificateDetails
     * @return encrypted CEK
     * @throws SQLServerException
     */
    private byte[] encryptRSAOAEP(byte[] plainText, CertificateDetails certificateDetails) throws AAPSDKException {
        byte[] cipherText = null;
        try {
            Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            rsa.init(Cipher.ENCRYPT_MODE, certificateDetails.certificate.getPublicKey());
            rsa.update(plainText);
            cipherText = rsa.doFinal();
        } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalBlockSizeException | NoSuchPaddingException
                | BadPaddingException e) {
            MessageFormat form = new MessageFormat(AAPSDKResource.getResource("R_EncryptionFailed"));
            Object[] msgArgs = {e.getMessage()};
            throw new AAPSDKException(form.format(msgArgs));
        }

        return cipherText;

    }

    private byte[] rsaSignHashedData(byte[] dataToSign, CertificateDetails certificateDetails) throws AAPSDKException {
        Signature signature;
        byte[] signedHash = null;

        try {
            signature = Signature.getInstance("SHA256withRSA");
            signature.initSign((PrivateKey) certificateDetails.privateKey);
            signature.update(dataToSign);
            signedHash = signature.sign();
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            MessageFormat form = new MessageFormat(AAPSDKResource.getResource("R_EncryptionFailed"));
            Object[] msgArgs = {e.getMessage()};
            throw new AAPSDKException(form.format(msgArgs));
        }
        return signedHash;

    }

    private byte[] getLittleEndianBytesFromShort(short value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] byteValue = byteBuffer.putShort(value).array();
        return byteValue;
    }

    /*
     * Verify signature against certificate
     */
    private boolean rsaVerifySignature(byte[] dataToVerify, byte[] signature,
            CertificateDetails certificateDetails) throws AAPSDKException {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign((PrivateKey) certificateDetails.privateKey);
            sig.update(dataToVerify);
            byte[] signedHash = sig.sign();

            sig.initVerify(certificateDetails.certificate.getPublicKey());
            sig.update(dataToVerify);

            return sig.verify(signedHash);

        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            MessageFormat form = new MessageFormat(AAPSDKResource.getResource("R_EncryptionFailed"));
            Object[] msgArgs = {e.getMessage()};
            throw new AAPSDKException(form.format(msgArgs));
        }
    }
}
