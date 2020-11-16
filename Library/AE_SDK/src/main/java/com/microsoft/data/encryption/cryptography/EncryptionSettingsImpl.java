package com.microsoft.data.encryption.cryptography;

public class EncryptionSettingsImpl extends EncryptionSettings {

    public Serializer<?> serializer;

    public Serializer<?> getSerializer() {
        return serializer;
    }

    private void setSerializer(Serializer<?> s) {
        serializer = s;
    }

    public EncryptionSettingsImpl(DataEncryptionKey d, Serializer<?> s) {
        this(d, GetDefaultEncryptionType(d), s);
    }

    public EncryptionSettingsImpl(DataEncryptionKey d, EncryptionType e, Serializer<?> s) {
        dataEncryptionKey = d;
        encryptionType = e;
        this.serializer = s;
    }

    @Override
    public ISerializer GetSerializer() {
        return this.serializer;
    }
}
