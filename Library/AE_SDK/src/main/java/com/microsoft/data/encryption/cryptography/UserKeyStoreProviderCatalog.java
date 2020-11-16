package com.microsoft.data.encryption.cryptography;

import java.util.concurrent.ConcurrentHashMap;


public class UserKeyStoreProviderCatalog<T> {
    private static UserKeyStoreProviderCatalog<?> instance = null;

    private ConcurrentHashMap<T, ConcurrentHashMap<String, EncryptionKeyStoreProvider>> keyStorage = new ConcurrentHashMap<T, ConcurrentHashMap<String, EncryptionKeyStoreProvider>>();

    private UserKeyStoreProviderCatalog() {}

    public static <T> UserKeyStoreProviderCatalog<?> getInstance() {
        if (instance == null) {
            instance = new UserKeyStoreProviderCatalog<T>();
        }

        return instance;
    }

    public void RegisterKeyStoreProvider(T userKey, EncryptionKeyStoreProvider encryptionKeyStoreProvider) {
        keyStorage.put(userKey, new ConcurrentHashMap<String, EncryptionKeyStoreProvider>());
    }

    public EncryptionKeyStoreProvider GetKeyStoreProvider(T userKey, String providerName) throws AAPSDKException {
        keyStorage.put(userKey, new ConcurrentHashMap<String, EncryptionKeyStoreProvider>());
        ConcurrentHashMap<String, EncryptionKeyStoreProvider> value = keyStorage.get(userKey);
        EncryptionKeyStoreProvider provider = value.get(userKey);
        if (provider != null) {
            return provider;
        }

        throw new AAPSDKException(AAPSDKResource.getResource("R_KeystoreProviderError"));
    }

    public void ClearUserProviders(T userKey) {
        keyStorage.remove(userKey);
    }

    public void Clear() {
        keyStorage.clear();
    }
}
