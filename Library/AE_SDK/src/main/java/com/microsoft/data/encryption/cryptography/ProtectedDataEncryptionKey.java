package com.microsoft.data.encryption.cryptography;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


public class ProtectedDataEncryptionKey extends DataEncryptionKey {

    public KeyEncryptionKey keyEncryptionKey;

    public KeyEncryptionKey getKeyEncryptionKey() {
        return keyEncryptionKey;
    }

    private void setKeyEncryptionKey(KeyEncryptionKey k) {
        keyEncryptionKey = k;
    }

    public byte[] encryptedValue;

    public byte[] getEncryptedValue() {
        return encryptedValue;
    }

    private void setEncryptedValue(byte[] b) {
        encryptedValue = b;
    }

    public static ProtectedDataEncryptionKey getOrCreate(String name, KeyEncryptionKey keyEncryptionKey,
            byte[] encryptedKey) throws Exception {
        Utils.validateNotNullOrWhitespace(name, "name");
        Utils.validateNotNull(keyEncryptionKey, "keyEncryptionKey");
        Utils.validateNotNull(encryptedKey, "encryptedKey");

        return new ProtectedDataEncryptionKey(name, keyEncryptionKey, encryptedKey);
    }

    public ProtectedDataEncryptionKey(String name,
            KeyEncryptionKey keyEncryptionKey) throws NoSuchAlgorithmException, Exception {
        this(name, keyEncryptionKey, generateNewColumnEncryptionKey(keyEncryptionKey));
    }

    public ProtectedDataEncryptionKey(String name, KeyEncryptionKey keyEncryptionKey,
            byte[] encryptedKey) throws Exception {
        super(name, keyEncryptionKey.decryptEncryptionKey(encryptedKey));

        Utils.validateNotNullOrWhitespace(name, "name");
        Utils.validateNotNull(keyEncryptionKey, "keyEncryptionKey");

        this.keyEncryptionKey = keyEncryptionKey;
        encryptedValue = encryptedKey;
    }

    public ProtectedDataEncryptionKey(String name, byte[] rootKey) throws Exception {
        super(name, rootKey);

        Utils.validateNotNullOrWhitespace(name, "name");
        Utils.validateNotNull(rootKey, "rootkey");
    }

    private static byte[] generateNewColumnEncryptionKey(KeyEncryptionKey masterKey) throws NoSuchAlgorithmException {
        byte[] plainTextColumnEncryptionKey = new byte[32];
        SecureRandom.getInstanceStrong().nextBytes(plainTextColumnEncryptionKey);
        return masterKey.encryptEncryptionKey(plainTextColumnEncryptionKey);
    }

    /// <inheritdoc/>
    public boolean equals(Object obj) {
        if (!(obj instanceof ProtectedDataEncryptionKey)) {
            return false;
        }

        ProtectedDataEncryptionKey other = (ProtectedDataEncryptionKey) obj;

        if (null == keyEncryptionKey && null != other.keyEncryptionKey) {
            return false;
        }

        return name.equals(other.name) && keyEncryptionKey.equals(other.keyEncryptionKey)
                && rootKeyHexString.equals(other.rootKeyHexString);
    }

    /// <inheritdoc/>
    @Override
    public int hashCode() {
        return new Triple<String, KeyEncryptionKey, String>(name, keyEncryptionKey, rootKeyHexString).hashCode();
    }
}
