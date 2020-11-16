package com.microsoft.data.encryption.cryptography;

public abstract class EncryptionSettings {
    public DataEncryptionKey dataEncryptionKey;

    public DataEncryptionKey getDataEncryptionKey() {
        return dataEncryptionKey;
    }

    protected void setDataEncryptionKey(DataEncryptionKey d) {
        if (null == d) {
            encryptionType = EncryptionType.Plaintext;
        }
        dataEncryptionKey = d;
    }

    public EncryptionType encryptionType = EncryptionType.Randomized;

    public EncryptionType getEncryptionType() {
        return encryptionType;
    }

    protected void setEncryptionType(EncryptionType e) {
        encryptionType = e;
    }

    public abstract ISerializer GetSerializer();

    protected static EncryptionType GetDefaultEncryptionType(DataEncryptionKey d) {
        return (null == d) ? EncryptionType.Plaintext : EncryptionType.Randomized;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof EncryptionSettings)) {
            return false;
        }

        if (null == dataEncryptionKey && null != ((EncryptionSettings) other).dataEncryptionKey) {
            return false;
        }

        return dataEncryptionKey.equals(((EncryptionSettings) other).dataEncryptionKey)
                && encryptionType.equals(((EncryptionSettings) other).encryptionType)
                && GetSerializer().getClass() == ((EncryptionSettings) other).GetSerializer().getClass();
    }
}
