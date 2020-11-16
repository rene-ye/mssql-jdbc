package com.microsoft.data.encryption.cryptography;

/**
 * 
 * Encryption types supported
 *
 */
public enum SqlServerEncryptionType {
    Deterministic((byte) 1),
    Randomized((byte) 2),
    PlainText((byte) 0);

    final byte value;
    private static final SqlServerEncryptionType[] VALUES = values();

    SqlServerEncryptionType(byte val) {
        this.value = val;
    }

    byte getValue() {
        return this.value;
    }

    static SqlServerEncryptionType of(byte val) throws AAPSDKException {
        for (SqlServerEncryptionType type : VALUES)
            if (val == type.value)
                return type;

        // Invalid type.
        throw new AAPSDKException();
    }
}
