package com.microsoft.data.encryption.cryptography;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Represents a single encrypted value for a CEK. It contains the encrypted CEK,the store type, name,the key path and
 * encryption algorithm.
 */
class EncryptionKeyInfo {
    EncryptionKeyInfo(byte[] encryptedKeyVal, int dbId, int keyId, int keyVersion, byte[] mdVersion, String keyPathVal,
            String keyStoreNameVal, String algorithmNameVal) {
        encryptedKey = encryptedKeyVal;
        databaseId = dbId;
        cekId = keyId;
        cekVersion = keyVersion;
        cekMdVersion = mdVersion;
        keyPath = keyPathVal;
        keyStoreName = keyStoreNameVal;
        algorithmName = algorithmNameVal;
    }

    byte[] encryptedKey; // the encrypted "column encryption key"
    int databaseId;
    int cekId;
    int cekVersion;
    byte[] cekMdVersion;
    String keyPath;
    String keyStoreName;
    String algorithmName;
    byte normalizationRuleVersion;
}


/**
 * Represents a unique CEK as an entry in the CekTable. A unique (plaintext is unique) CEK can have multiple encrypted
 * CEKs when using multiple CMKs. These encrypted CEKs are represented by a member ArrayList.
 */
class CekTableEntry {
    static final private java.util.logging.Logger aeLogger = java.util.logging.Logger
            .getLogger("com.microsoft.sqlserver.jdbc.AE");

    List<EncryptionKeyInfo> columnEncryptionKeyValues;
    int ordinal;
    int databaseId;
    int cekId;
    int cekVersion;
    byte[] cekMdVersion;

    List<EncryptionKeyInfo> getColumnEncryptionKeyValues() {
        return columnEncryptionKeyValues;
    }

    int getOrdinal() {
        return ordinal;
    }

    int getDatabaseId() {
        return databaseId;
    }

    int getCekId() {
        return cekId;
    }

    int getCekVersion() {
        return cekVersion;
    }

    byte[] getCekMdVersion() {
        return cekMdVersion;
    }

    CekTableEntry(int ordinalVal) {
        ordinal = ordinalVal;
        databaseId = 0;
        cekId = 0;
        cekVersion = 0;
        cekMdVersion = null;
        columnEncryptionKeyValues = new ArrayList<>();
    }

    int getSize() {
        return columnEncryptionKeyValues.size();
    }

    void add(byte[] encryptedKey, int dbId, int keyId, int keyVersion, byte[] mdVersion, String keyPath,
            String keyStoreName, String algorithmName) {

        assert null != columnEncryptionKeyValues : "columnEncryptionKeyValues should already be initialized.";

        aeLogger.fine("Retrieving CEK values");

        EncryptionKeyInfo encryptionKey = new EncryptionKeyInfo(encryptedKey, dbId, keyId, keyVersion, mdVersion,
                keyPath, keyStoreName, algorithmName);
        columnEncryptionKeyValues.add(encryptionKey);

        if (0 == databaseId) {
            databaseId = dbId;
            cekId = keyId;
            cekVersion = keyVersion;
            cekMdVersion = mdVersion;
        } else {
            assert (databaseId == dbId);
            assert (cekId == keyId);
            assert (cekVersion == keyVersion);
            assert ((null != cekMdVersion) && (null != mdVersion) && (cekMdVersion.length == mdVersion.length));
        }
    }
}


/**
 * Contains all CEKs, each row represents one unique CEK (represented by CekTableEntry).
 */
class CekTable implements Serializable {
    /**
     * Always update serialVersionUID when prompted.
     */
    private static final long serialVersionUID = -4568542970907052239L;

    CekTableEntry[] keyList;

    CekTable(int tableSize) {
        keyList = new CekTableEntry[tableSize];
    }

    int getSize() {
        return keyList.length;
    }

    CekTableEntry getCekTableEntry(int index) {
        return keyList[index];
    }

    void setCekTableEntry(int index, CekTableEntry entry) {
        keyList[index] = entry;
    }
}


// Fields in the first resultset of "sp_describe_parameter_encryption"
// We expect the server to return the fields in the resultset in the same order as mentioned below.
// If the server changes the below order, then transparent parameter encryption will break.
enum DescribeParameterEncryptionResultSet1 {
    KeyOrdinal,
    DbId,
    KeyId,
    KeyVersion,
    KeyMdVersion,
    EncryptedKey,
    ProviderName,
    KeyPath,
    KeyEncryptionAlgorithm,
    IsRequestedByEnclave,
    EnclaveCMKSignature;

    int value() {
        // Column indexing starts from 1;
        return ordinal() + 1;
    }
}


// Fields in the second resultset of "sp_describe_parameter_encryption"
// We expect the server to return the fields in the resultset in the same order as mentioned below.
// If the server changes the below order, then transparent parameter encryption will break.
enum DescribeParameterEncryptionResultSet2 {
    ParameterOrdinal,
    ParameterName,
    ColumnEncryptionAlgorithm,
    ColumnEncrytionType,
    ColumnEncryptionKeyOrdinal,
    NormalizationRuleVersion;

    int value() {
        // Column indexing starts from 1;
        return ordinal() + 1;
    }
}


enum ColumnEncryptionVersion {
    AE_NotSupported,
    AE_v1,
    AE_v2;

    int value() {
        // Column indexing starts from 1;
        return ordinal() + 1;
    }
}
