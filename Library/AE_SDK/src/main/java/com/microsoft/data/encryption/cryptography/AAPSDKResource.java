package com.microsoft.data.encryption.cryptography;

import java.util.ListResourceBundle;


/**
 * Represents a simple resource bundle containing the strings for localizing.
 *
 */
public final class AAPSDKResource extends ListResourceBundle {
    static String getResource(String key) {
        return AAPSDKResource.getBundle("com.microsoft.data.encryption.cryptography.AAPSDKResource").getString(key);
    }

    protected Object[][] getContents() {
        return CONTENTS;
    }

    static final Object[][] CONTENTS = {{"R_EncryptionFailed", "Internal error while encryption:  {0} "},
            {"R_DecryptionFailed", "Internal error while decryption:  {0} "},
            {"R_NullEncryptedColumnEncryptionKey", "Encrypted column encryption key cannot be null."},
            {"R_NullEncryptionSettings", "Encryption settings cannot be null."},
            {"R_PlaintextEncryptionSettings", "Encryption settings cannot be Plaintext in this context."},
            {"R_InvalidCipherTextSize",
                    "Specified ciphertext has an invalid size of {0} bytes, which is below the minimum {1} bytes required for decryption."},
            {"R_InvalidAlgorithmVersion",
                    "The specified ciphertext''s encryption algorithm version {0} does not match the expected encryption algorithm version {1} ."},
            {"R_InvalidColumnNumbers", "Number of columns do not match the encryption settings count."},
            {"R_InvalidDataEncryptionKey",
                    "DataEncryptionKey name cannot be null or empty or consist of only whitespace."},
            {"R_InvalidKeySize", "key must contain {0} elements."},

            {"R_IllegalOffset", "Illegal offset minutes."}, {"R_IllegalNanos", "Illegal nanosecond values."},
            {"R_NullColumnEncryptionKey", "Column encryption key cannot be null."},
            {"R_EmptyColumnEncryptionKey", "Empty column encryption key specified."},
            {"R_InvalidAlgorithm", "SHA-256 Algorithm is not supported."},
            {"R_InvalidMasterKey", "Invalid master key details specified."},
            {"R_InvalidKeyStorePath",
                    "System cannot find the key store file at the specified path. Verify that the path is correct and you have proper permissions to access it."},
            {"R_InvalidFileFormat",
                    "Cannot parse \"{0}\". Either the file format is not valid or the password is not correct."},
            {"R_CertificateNotFoundForAlias",
                    "Certificate with alias \"{0}\" not found in the store provided by \"{1}\". Verify the certificate has been imported correctly into the certificate location/store."},
            {"R_UnrecoverableKeyAE",
                    "Cannot recover private key from keystore with certificate details \"{0}\". Verify that imported certificate for Always Encrypted contains private key and password provided for certificate is correct."},
            {"R_CertificateError", "Error occurred while retrieving certificate \"{0}\" from keystore \"{1}\"."},
            {"R_NullKeyEncryptionAlgorithm", "Key encryption algorithm cannot be null."},
            {"R_InvalidKeyEncryptionAlgorithm",
                    "Invalid key encryption algorithm specified: {0}. Expected value: {1}."},
            {"R_InvalidMasterKeyDetails", "Invalid master key details specified."},
            {"R_EncryptedCEKNull", "Internal error. Encrypted column encryption key cannot be null."},
            {"R_EmptyEncryptedCEK", "Internal error. Empty encrypted column encryption key specified."},
            {"R_CEKSignatureNotMatchCMK",
                    "The specified encrypted column encryption key signature does not match the signature computed with the column master key (certificate) in \"{0}\". The encrypted column encryption key may be corrupt, or the specified path may be incorrect."},
            {"R_CEKDecryptionFailed", "Exception while decryption of encrypted column encryption key:  {0} "},
            {"R_ByteToShortConversion", "Error occurred while decrypting column encryption key."},
            {"R_InvalidValueToType", "The given value from the data source cannot be converted to type {0}."},
            {"R_InvalidSerializerName", "There is no serializer that maps to the class name {0}. "},
            {"R_InvalidDataType", "Invalid data type provided."},
            {"R_InvalidType", "Encryption and decryption of data type {0} is not supported."},
            {"R_DecryptionFailed", "Decryption of the data type {0} failed. Normalization error."},
            {"R_InvalidEncoding", "The encoding {0} is not supported."},
            {"R_NullString", "Null strings are not accepted"},
            {"R_InvalidTimestampFormat", "Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]"},
            {"R_InvalidTemporalValue", "Invalid temporal value provided."},
            {"R_UnexpectedSSType", "Unexpected SSType: {0}."}, {"R_UnexpectedJDBCType", "Unexpected JDBCType: {0}."},
            {"R_InvalidTemporalValue", "Invalid temporal value provided."},
            {"R_KeystoreProviderError", "An error has occured while getting keystore provider."},
            {"R_CannotBeNull", "{0} cannot be null."}, {"R_CannotBeNullOrWhiteSpace", "{0} cannot be null or empty."},
            {"R_InvalidPSLength",
                    "Value {0} of type {1} is invalid for the expected precision of {2} and scale of {3}."},
            {"R_InvalidKeyStoreLocation", "Location of keystore {0} is invalid."},
            {"R_valueOutOfRange", "One or more values is out of range of values for the {0} data type."},};
};
