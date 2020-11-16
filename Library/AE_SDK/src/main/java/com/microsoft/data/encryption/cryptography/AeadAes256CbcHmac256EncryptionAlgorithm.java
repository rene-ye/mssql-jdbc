package com.microsoft.data.encryption.cryptography;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class AeadAes256CbcHmac256EncryptionAlgorithm extends DataProtector {
    final static String algorithmName = "AEAD_AES_256_CBC_HMAC_SHA256";
    // Stores column encryption key which includes root key and derived keys
    private DataEncryptionKey columnEncryptionkey;
    private byte algorithmVersion = 0x1;
    // This variable indicate whether encryption type is deterministic (if true)
    // or random (if false)
    private boolean isDeterministic = false;
    // Each block in the AES is 128 bits
    private int blockSizeInBytes = 16;
    private int keySizeInBits = 256;
    private int keySizeInBytes = keySizeInBits / 8;
    private byte[] version = new byte[] {0x01};
    // Added so that java hashing algorithm is similar to c#
    private byte[] versionSize = new byte[] {1};

    /*
     * Minimum Length of cipherText without authentication tag. This value is 1 (version byte) + 16 (IV) + 16 (minimum
     * of 1 block of cipher Text)
     */
    private int minimumCipherTextLengthInBytesNoAuthenticationTag = 1 + blockSizeInBytes + blockSizeInBytes;

    /*
     * Minimum Length of cipherText. This value is 1 (version byte) + 32 (authentication tag) + 16 (IV) + 16 (minimum of
     * 1 block of cipher Text)
     */
    private int minimumCipherTextLengthInBytesWithAuthenticationTag = minimumCipherTextLengthInBytesNoAuthenticationTag
            + keySizeInBytes;

    private static Map<Tuple<DataEncryptionKey, EncryptionType>, AeadAes256CbcHmac256EncryptionAlgorithm> algorithmCache = new ConcurrentHashMap<Tuple<DataEncryptionKey, EncryptionType>, AeadAes256CbcHmac256EncryptionAlgorithm>();

    /**
     * Retrieves existing AeadAes256CbcHmac256EncryptionAlgorithm or creates a new
     * AeadAes256CbcHmac256EncryptionAlgorithm.
     * 
     * @param dataEncryptionKey
     *        data encryption key
     * @param encryptionType
     *        encryption type
     * @return AeadAes256CbcHmac256EncryptionAlgorithm instance
     * @throws AAPSDKException
     */
    public static AeadAes256CbcHmac256EncryptionAlgorithm getOrCreate(DataEncryptionKey dataEncryptionKey,
            EncryptionType encryptionType) throws AAPSDKException {
        if (null == dataEncryptionKey) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_NullColumnEncryptionKey"));
        }

        Tuple<DataEncryptionKey, EncryptionType> key = new Tuple<DataEncryptionKey, EncryptionType>(dataEncryptionKey,
                encryptionType);

        if (algorithmCache.containsKey(key)) {
            return algorithmCache.get(key);
        } else {
            AeadAes256CbcHmac256EncryptionAlgorithm e = new AeadAes256CbcHmac256EncryptionAlgorithm(dataEncryptionKey,
                    encryptionType);
            algorithmCache.put(key, e);
            return e;
        }
    }

    /**
     * Constructor for AeadAes256CbcHmac256EncryptionAlgorithm
     * 
     * @param dataEncryptionKey
     *        data encryption key
     * @param encryptionType
     *        encryption type
     */
    public AeadAes256CbcHmac256EncryptionAlgorithm(DataEncryptionKey dataEncryptionKey, EncryptionType encryptionType) {
        this.columnEncryptionkey = dataEncryptionKey;
        if (encryptionType == EncryptionType.Deterministic) {
            this.isDeterministic = true;
        }
    }

    /**
     * Encrypts plaintext byte of arrays
     */
    public byte[] Encrypt(byte[] plaintext) throws AAPSDKException {
        return encryptData(plaintext, true);
    }

    /**
     * Performs encryption of plain text
     * 
     * @param plainText
     *        text to be encrypted
     * @param hasAuthenticationTag
     *        specify if encryption needs authentication
     * @return cipher text
     * @throws SQLServerException
     */
    protected byte[] encryptData(byte[] plainText, boolean hasAuthenticationTag) throws AAPSDKException {
        // we will generate this initialization vector based whether
        // this encryption type is deterministic
        assert (plainText != null);
        byte[] iv = new byte[blockSizeInBytes];
        // Secret/private key to be used in AES encryption
        // TODO: do I call columnEncryptionkey.getEncryptionKeyBytes() or columnEncryptionkey.getRootKeyBytes here?
        SecretKeySpec skeySpec = new SecretKeySpec(columnEncryptionkey.getEncryptionKeyBytes(), "AES");

        if (isDeterministic) {
            // this method makes sure this is 16 bytes key
            try {
                iv = SqlServerSecurityUtility.getHMACWithSHA256(plainText, columnEncryptionkey.getIvKeyBytes(),
                        blockSizeInBytes);
            } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                MessageFormat form = new MessageFormat(AAPSDKResource.getResource("R_EncryptionFailed"));
                Object[] msgArgs = {e.getMessage()};
                throw new AAPSDKException(form.format(msgArgs));
            }
        } else {
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
        }

        int numBlocks = plainText.length / blockSizeInBytes + 1;

        int hmacStartIndex = 1;
        int authenticationTagLen = hasAuthenticationTag ? keySizeInBytes : 0;
        int ivStartIndex = hmacStartIndex + authenticationTagLen;
        int cipherStartIndex = ivStartIndex + blockSizeInBytes;

        // Output buffer size = size of VersionByte + Authentication Tag + IV + cipher Text blocks.
        int outputBufSize = 1 + authenticationTagLen + iv.length + (numBlocks * blockSizeInBytes);
        byte[] outBuffer = new byte[outputBufSize];

        // Copying the version to output buffer
        outBuffer[0] = algorithmVersion;
        // Coping IV to the output buffer
        System.arraycopy(iv, 0, outBuffer, ivStartIndex, iv.length);

        // Start the AES encryption

        try {
            // initialization vector
            IvParameterSpec ivector = new IvParameterSpec(iv);
            Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivector);

            int count = 0;
            int cipherIndex = cipherStartIndex; // this is where cipherText starts

            if (numBlocks > 1) {
                count = (numBlocks - 1) * blockSizeInBytes;
                cipherIndex += encryptCipher.update(plainText, 0, count, outBuffer, cipherIndex);
            }
            // doFinal will complete the encryption
            byte[] buffTmp = encryptCipher.doFinal(plainText, count, plainText.length - count);
            // Encryption completed
            System.arraycopy(buffTmp, 0, outBuffer, cipherIndex, buffTmp.length);

            if (hasAuthenticationTag) {

                Mac hmac = Mac.getInstance("HmacSHA256");
                SecretKeySpec initkey = new SecretKeySpec(columnEncryptionkey.getMacKeyBytes(), "HmacSHA256");
                hmac.init(initkey);
                hmac.update(version, 0, version.length);
                hmac.update(iv, 0, iv.length);
                hmac.update(outBuffer, cipherStartIndex, numBlocks * blockSizeInBytes);
                hmac.update(versionSize, 0, version.length);
                byte[] hash = hmac.doFinal();
                // coping the authentication tag in the output buffer which holds cipher text
                System.arraycopy(hash, 0, outBuffer, hmacStartIndex, authenticationTagLen);

            }
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException
                | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | ShortBufferException e) {
            MessageFormat form = new MessageFormat(AAPSDKResource.getResource("R_EncryptionFailed"));
            Object[] msgArgs = {e.getMessage()};
            throw new AAPSDKException(form.format(msgArgs));
        }

        return outBuffer;

    }

    /**
     * Decrypts already encrypted ciphertext
     */
    public byte[] Decrypt(byte[] ciphertext) throws AAPSDKException {
        return decryptData(ciphertext, true);
    }

    /**
     * Decrypt the cipher text and return plain text
     * 
     * @param cipherText
     *        data to be decrypted
     * @param hasAuthenticationTag
     *        tells whether cipher text contain authentication tag
     * @return plain text
     * @throws SQLServerException
     */
    private byte[] decryptData(byte[] cipherText, boolean hasAuthenticationTag) throws AAPSDKException {
        assert (cipherText != null);

        byte[] iv = new byte[blockSizeInBytes];

        int minimumCipherTextLength = hasAuthenticationTag ? minimumCipherTextLengthInBytesWithAuthenticationTag
                                                           : minimumCipherTextLengthInBytesNoAuthenticationTag;

        // Here we check if length of cipher text is more than minimum value,
        // if not exception is thrown
        if (cipherText.length < minimumCipherTextLength) {
            throw new AAPSDKException();
        }

        // Validate the version byte
        int startIndex = 0;
        if (cipherText[startIndex] != algorithmVersion) {
            throw new AAPSDKException();

        }

        startIndex += 1;
        int authenticationTagOffset = 0;

        // Read authentication tag
        if (hasAuthenticationTag) {
            authenticationTagOffset = startIndex;
            // authentication tag size is keySizeInBytes
            startIndex += keySizeInBytes;
        }

        // Read IV from cipher text
        System.arraycopy(cipherText, startIndex, iv, 0, iv.length);
        startIndex += iv.length;

        // To read encrypted text from cipher
        int cipherTextOffset = startIndex;
        // All data after IV is encrypted data
        int cipherTextCount = cipherText.length - startIndex;

        if (hasAuthenticationTag) {
            byte[] authenticationTag;
            try {
                authenticationTag = prepareAuthenticationTag(iv, cipherText, cipherTextOffset, cipherTextCount);
            } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                MessageFormat form = new MessageFormat(AAPSDKResource.getResource("R_InvalidCipherTextSize"));
                Object[] msgArgs = {cipherText.length, minimumCipherTextLength};
                throw new AAPSDKException(form.format(msgArgs));
            }
            if (!(SqlServerSecurityUtility.compareBytes(authenticationTag, cipherText, authenticationTagOffset,
                    cipherTextCount))) {
                MessageFormat form = new MessageFormat(AAPSDKResource.getResource("R_InvalidAlgorithmVersion"));
                // converting byte to Hexa Decimal
                Object[] msgArgs = {String.format("%02X ", cipherText[startIndex]),
                        String.format("%02X ", algorithmVersion)};
                throw new AAPSDKException(form.format(msgArgs));
            }

        }

        // Decrypt the text and return
        return decryptData(iv, cipherText, cipherTextOffset, cipherTextCount);
    }

    /**
     * Decrypt data with specified IV
     * 
     * @param iv
     *        initialization vector
     * @param cipherText
     *        text to be decrypted
     * @param offset
     *        of cipher text
     * @param count
     *        length of cipher text
     * @return plain text
     * @throws SQLServerException
     */
    private byte[] decryptData(byte[] iv, byte[] cipherText, int offset, int count) throws AAPSDKException {
        assert (cipherText != null);
        assert (iv != null);
        byte[] plainText = null;
        // key to be used for decryption
        SecretKeySpec skeySpec = new SecretKeySpec(columnEncryptionkey.getEncryptionKeyBytes(), "AES");
        IvParameterSpec ivector = new IvParameterSpec(iv);
        Cipher decryptCipher;
        try {
            decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decryptCipher.init(Cipher.DECRYPT_MODE, skeySpec, ivector);
            plainText = decryptCipher.doFinal(cipherText, offset, count);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException
                | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            MessageFormat form = new MessageFormat(AAPSDKResource.getResource("R_DecryptionFailed"));
            Object[] msgArgs = {e.getMessage()};
            throw new AAPSDKException(form.format(msgArgs));
        }

        return plainText;

    }

    /**
     * Prepare the authentication tag
     * 
     * @param iv
     *        initialization vector
     * @param cipherText
     * @param offset
     * @param length
     *        length of cipher text
     * @return authentication tag
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private byte[] prepareAuthenticationTag(byte[] iv, byte[] cipherText, int offset,
            int length) throws NoSuchAlgorithmException, InvalidKeyException {
        assert (cipherText != null);
        byte[] computedHash;
        byte[] authenticationTag = new byte[keySizeInBytes];

        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(columnEncryptionkey.getMacKeyBytes(), "HmacSHA256");
        hmac.init(key);
        hmac.update(version, 0, version.length);
        hmac.update(iv, 0, iv.length);
        hmac.update(cipherText, offset, length);
        hmac.update(versionSize, 0, version.length);
        computedHash = hmac.doFinal();
        System.arraycopy(computedHash, 0, authenticationTag, 0, authenticationTag.length);

        return authenticationTag;
    }
}
