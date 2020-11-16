package com.microsoft.data.encryption.cryptography;

public abstract class FileEncryptionSettings extends EncryptionSettings {

    public ProtectedDataEncryptionKey dataEncryptionKey;

    public ProtectedDataEncryptionKey getDataEncryptionKey() {
        return (ProtectedDataEncryptionKey) super.dataEncryptionKey;
    }

    protected void setProtectedDataEncryptionKey(ProtectedDataEncryptionKey p) {
        super.dataEncryptionKey = p;
    }
}
