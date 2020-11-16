package com.microsoft.data.encryption.cryptography;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 
 * Defines the abtract class for a SQL Server Column Encryption key store provider Extend this class to implement a
 * custom key store provider.
 *
 */
public abstract class EncryptionKeyStoreProvider implements IEncryptionKeyStoreProvider {

    protected Map<String, byte[]> dataEncryptionKeyCache = new ConcurrentHashMap<String, byte[]>();

    protected Map<Quadruple<String, String, Boolean, String>, Boolean> keyEncryptionKeyMetadataSignatureVerificationCache = new ConcurrentHashMap<Quadruple<String, String, Boolean, String>, Boolean>() {};

    public String providerName;

    public String getProviderName() {
        return providerName;
    }

    public abstract byte[] unwrapKey(String encryptionKeyId, KeyEncryptionKeyAlgorithm algorithm, byte[] encryptedKey);

    public abstract byte[] wrapKey(String encryptionKeyId, KeyEncryptionKeyAlgorithm algorithm, byte[] key);

    public abstract byte[] sign(String encryptionKeyId, boolean allowEnclaveComputations);

    public abstract boolean verify(String encryptionKeyId, boolean allowEnclaveComputations, byte[] signature);

    protected byte[] getOrCreateDataEncryptionKey(String encryptedDataEncryptionKey, byte[] createItem) {
        return dataEncryptionKeyCache.put(encryptedDataEncryptionKey, createItem);
    }

    protected boolean getOrCreateSignatureVerificationResult(Quadruple<String, String, Boolean, String> keyInformation,
            boolean createItem) {
        return keyEncryptionKeyMetadataSignatureVerificationCache.put(keyInformation, createItem);
    }
}
