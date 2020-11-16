package com.microsoft.data.encryption.cryptography;

import java.util.HashSet;
import java.util.List;


public class CryptoMetadata {
    public HashSet<ColumnEncryptionMetadata> ColumnEncryptionInformation;
    public HashSet<EncryptionKeyMetadata> ColumnKeyInformation;
    public HashSet<MasterKeyMetadata> ColumnMasterKeyInformation;

    public boolean IsEmpty() {
        return ColumnEncryptionInformation.isEmpty();
    }

    public static CryptoMetadata CompileMetadata(List<IColumn> columns,
            List<FileEncryptionSettings> encryptionSettings) throws AAPSDKException {
        if (columns.size() != encryptionSettings.size()) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_InvalidColumnNumbers"));
        }

        CryptoMetadata cryptoMetadata = new CryptoMetadata();

        for (int i = 0; i < columns.size(); i++) {
            FileEncryptionSettings settings = encryptionSettings.get(i);

            if (settings.encryptionType != EncryptionType.Plaintext) {
                ColumnEncryptionMetadata columnEncryptionInformation = new ColumnEncryptionMetadata(columns.get(i).name,
                        encryptionSettings.indexOf(settings), settings.dataEncryptionKey.name, settings.encryptionType,
                        settings.GetSerializer());
                EncryptionKeyMetadata columnKeyInformation = new EncryptionKeyMetadata(settings.dataEncryptionKey.name,
                        bytesToHex(settings.dataEncryptionKey.encryptedValue),
                        settings.dataEncryptionKey.keyEncryptionKey.name);
                MasterKeyMetadata columnMasterKeyInformation = new MasterKeyMetadata(
                        settings.dataEncryptionKey.keyEncryptionKey.name,
                        settings.dataEncryptionKey.keyEncryptionKey.KeyStoreProvider.providerName,
                        settings.dataEncryptionKey.keyEncryptionKey.path);

                cryptoMetadata.ColumnEncryptionInformation.add(columnEncryptionInformation);
                cryptoMetadata.ColumnKeyInformation.add(columnKeyInformation);
                cryptoMetadata.ColumnMasterKeyInformation.add(columnMasterKeyInformation);
            }
        }

        return cryptoMetadata;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}


class ColumnEncryptionMetadata {
    public String columnName;
    public int columnIndex;
    public String columnKeyName;
    public EncryptionType encryptionType;
    public ISerializer serializer;

    public ColumnEncryptionMetadata(String columnName, int columnIndex, String columnKeyName,
            EncryptionType encryptionType, ISerializer serializer) {
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        this.columnKeyName = columnKeyName;
        this.encryptionType = encryptionType;
        this.serializer = serializer;
    }

    @Override
    public boolean equals(Object obj) {
        ColumnEncryptionMetadata c = (ColumnEncryptionMetadata) obj;
        return (null != c) && columnName == c.columnName && columnIndex == c.columnIndex;
    }
}


class EncryptionKeyMetadata {
    public String name;
    public String encryptedColumnKey;
    public String columnMasterKeyName;

    public EncryptionKeyMetadata(String name, String encryptedColumnKey, String columnMasterKeyName) {
        this.name = name;
        this.encryptedColumnKey = encryptedColumnKey;
        this.columnMasterKeyName = columnMasterKeyName;
    }

    @Override
    public boolean equals(Object obj) {
        EncryptionKeyMetadata c = (EncryptionKeyMetadata) obj;
        return (null != c) && name == c.name;
    }
}


class MasterKeyMetadata {
    public String Name;
    public String KeyProvider;
    public String KeyPath;

    MasterKeyMetadata(String n, String kProvider, String kPath) {
        Name = n;
        KeyProvider = kProvider;
        KeyPath = kPath;
    }

    @Override
    public boolean equals(Object obj) {
        MasterKeyMetadata c = (MasterKeyMetadata) obj;
        return !(null == c) && Name == c.Name;
    }
}
