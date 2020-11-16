package com.microsoft.data.encryption.cryptography;

public interface IEncryptionKeyStoreProvider {

    /**
     * Decrypts the specified encrypted value of a column encryption key. The encrypted value is expected to be
     * encrypted using the column master key with the specified key path and using the specified algorithm.
     * 
     * @param masterKeyPath
     *        The column master key path.
     * @param encryptionAlgorithm
     *        the specific encryption algorithm.
     * @param encryptedColumnEncryptionKey
     *        the encrypted column encryption key
     * @return the decrypted value of column encryption key.
     * @throws SQLServerException
     *         when an error occurs while decrypting the CEK
     */
    byte[] decryptEncryptionKey(String masterKeyPath, String encryptionAlgorithm,
            byte[] encryptedColumnEncryptionKey) throws AAPSDKException;

    /**
     * Encrypts a column encryption key using the column master key with the specified key path and using the specified
     * algorithm.
     * 
     * @param masterKeyPath
     *        The column master key path.
     * @param encryptionAlgorithm
     *        the specific encryption algorithm.
     * @param columnEncryptionKey
     *        column encryption key to be encrypted.
     * @return the encrypted column encryption key.
     * @throws SQLServerException
     *         when an error occurs while encrypting the CEK
     */
    byte[] encryptEncryptionKey(String masterKeyPath, String encryptionAlgorithm,
            byte[] columnEncryptionKey) throws AAPSDKException;

    byte[] signMasterKeyMetadata(String masterKeyPath, boolean allowEnclaveComputations) throws AAPSDKException;

    /**
     * Verify the signature is valid for the column master key
     * 
     * @param masterKeyPath
     *        column master key path
     * @param allowEnclaveComputations
     *        indicates whether the column master key supports enclave computations
     * @param signature
     *        signature of the column master key metadata
     * @return whether the signature is valid for the column master key
     * @throws SQLServerException
     *         when an error occurs while verifying the signature
     */
    boolean verifyMasterKeyMetadata(String masterKeyPath, boolean allowEnclaveComputations,
            byte[] signature) throws AAPSDKException;
}
