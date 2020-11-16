package com.microsoft.data.encryption.cryptography;

public class KeyEncryptionKey {
    private static final KeyEncryptionKeyAlgorithm encryptionAlgorithm = KeyEncryptionKeyAlgorithm.RSA_OAEP;
    private byte[] signature;

    public String name;

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }

    public EncryptionKeyStoreProvider KeyStoreProvider;

    public EncryptionKeyStoreProvider getKeyStoreProvider() {
        return KeyStoreProvider;
    }

    public void setKeyStoreProvider(EncryptionKeyStoreProvider e) {
        KeyStoreProvider = e;
    }

    public String path;

    public String getPath() {
        return path;
    }

    public void setPath(String s) {
        path = s;
    }

    public boolean IsEnclaveSupported;

    public boolean getIsEnclaveSupported() {
        return IsEnclaveSupported;
    }

    public void setIsEnclaveSupported(boolean b) {
        IsEnclaveSupported = b;
    }

    public byte[] Signature;

    public byte[] getSignature() {
        return Signature;
    }

    public static KeyEncryptionKey getOrCreate(String name, String path, EncryptionKeyStoreProvider keystore,
            boolean isEnclaveSupported) throws Exception {
        Utils.validateNotNullOrWhitespace(name, "name");
        Utils.validateNotNullOrWhitespace(path, "path");
        Utils.validateNotNull(keystore, "keystore");

        return new KeyEncryptionKey(name, path, keystore, isEnclaveSupported);
    }

    public KeyEncryptionKey(String name, String path, EncryptionKeyStoreProvider keystore,
            boolean isEnclaveSupported) throws Exception {
        Utils.validateNotNullOrWhitespace(name, "name");
        Utils.validateNotNullOrWhitespace(path, "path");
        Utils.validateNotNull(keystore, "keystore");

        this.name = name;
        this.path = path;
        KeyStoreProvider = keystore;
        IsEnclaveSupported = isEnclaveSupported;
        signature = KeyStoreProvider.sign(path, isEnclaveSupported);
    }

    public byte[] encryptEncryptionKey(byte[] plaintextEncryptionKey) {
        return KeyStoreProvider.wrapKey(path, encryptionAlgorithm, plaintextEncryptionKey);
    }

    public byte[] decryptEncryptionKey(byte[] encryptedEncryptionKey) {
        return KeyStoreProvider.unwrapKey(path, encryptionAlgorithm, encryptedEncryptionKey);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof KeyEncryptionKey)) {
            return false;
        }

        KeyEncryptionKey other = (KeyEncryptionKey) obj;

        return name.equals(other.name) && KeyStoreProvider.equals(other.KeyStoreProvider) && path.equals(other.path)
                && IsEnclaveSupported == (other.IsEnclaveSupported);
    }

    @Override
    public int hashCode() {
        return new Quadruple<String, EncryptionKeyStoreProvider, String, Boolean>(name, KeyStoreProvider, path,
                IsEnclaveSupported).hashCode();
    }
}
