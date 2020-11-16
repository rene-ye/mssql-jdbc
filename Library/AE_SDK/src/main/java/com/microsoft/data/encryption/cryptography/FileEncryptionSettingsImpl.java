package com.microsoft.data.encryption.cryptography;

public class FileEncryptionSettingsImpl extends FileEncryptionSettings {
    public Serializer<?> serializer;

    public Serializer<?> getSerializer() {
        return serializer;
    }

    public FileEncryptionSettingsImpl(ProtectedDataEncryptionKey d, Serializer<?> s) {
        this(d, GetDefaultEncryptionType(d), s);
    }

    public FileEncryptionSettingsImpl(ProtectedDataEncryptionKey d, EncryptionType e, Serializer<?> s) {
        dataEncryptionKey = d;
        encryptionType = e;
        this.serializer = s;
    }

    @Override
    public ISerializer GetSerializer() {
        return this.serializer;
    }
}
