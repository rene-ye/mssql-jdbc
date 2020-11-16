package com.microsoft.data.encryption.cryptography;

import static java.nio.charset.StandardCharsets.UTF_16LE;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


abstract class DataEncryptionKey {
    protected String rootKeyHexString;
    private final int KEY_SIZE_IN_BITS = 256;
    public final int KEY_SIZE_IN_BYTES = KEY_SIZE_IN_BITS / 8;

    public String name;
    private byte[] rootKeyBytes;
    private byte[] encryptionKeyBytes;
    private byte[] macKeyBytes;
    private byte[] ivKeyBytes;

    protected DataEncryptionKey(String name,
            byte[] rootKey) throws AAPSDKException, InvalidKeyException, NoSuchAlgorithmException {
        if (name == null || name.trim().isEmpty()) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_InvalidDataEncryptionKey"));
        }

        if (KEY_SIZE_IN_BYTES != rootKey.length) {
            // KEY_SIZE_IN_BYTES
            throw new AAPSDKException(AAPSDKResource.getResource("R_InvalidKeySize"));
        }

        this.name = name;

        String encryptionKeySalt = "Microsoft SQL Server cell encryption key with encryption algorithm:AEAD_AES_256_CBC_HMAC_SHA256 and key length:"
                + KEY_SIZE_IN_BITS;
        String macKeySalt = "Microsoft SQL Server cell MAC key with encryption algorithm:AEAD_AES_256_CBC_HMAC_SHA256 and key length:"
                + KEY_SIZE_IN_BITS;
        String ivKeySalt = "Microsoft SQL Server cell IV key with encryption algorithm:AEAD_AES_256_CBC_HMAC_SHA256 and key length:"
                + KEY_SIZE_IN_BITS;

        byte[] encryptionKeyBytes = SqlServerSecurityUtility.getHMACWithSHA256(encryptionKeySalt.getBytes(UTF_16LE),
                rootKey, KEY_SIZE_IN_BYTES);
        byte[] macKeyBytes = SqlServerSecurityUtility.getHMACWithSHA256(macKeySalt.getBytes(UTF_16LE), rootKey,
                KEY_SIZE_IN_BYTES);
        byte[] ivKeyBytes = SqlServerSecurityUtility.getHMACWithSHA256(ivKeySalt.getBytes(UTF_16LE), rootKey,
                KEY_SIZE_IN_BYTES);

        this.rootKeyBytes = rootKey;
        this.encryptionKeyBytes = encryptionKeyBytes;
        this.macKeyBytes = macKeyBytes;
        this.ivKeyBytes = ivKeyBytes;
    }

    public String getName() {
        return name;
    }

    protected void setName(String value) {
        name = value;
    }

    public byte[] getRootKeyBytes() {
        return rootKeyBytes;
    }

    public void setRootKeyBytes(byte[] value) {
        rootKeyBytes = value;
        rootKeyHexString = CryptographyExtensions.ToBase64String(value);
    }

    public String getRootKeyHexString() {
        return rootKeyHexString;
    }

    public byte[] getEncryptionKeyBytes() {
        return encryptionKeyBytes;
    }

    public void setEncryptionKeyBytes(byte[] value) {
        encryptionKeyBytes = value;
    }

    public byte[] getMacKeyBytes() {
        return macKeyBytes;
    }

    public void setMacKeyBytes(byte[] value) {
        macKeyBytes = value;
    }

    public byte[] getIvKeyBytes() {
        return ivKeyBytes;
    }

    public void setIvKeyBytes(byte[] value) {
        ivKeyBytes = value;
    }
}
