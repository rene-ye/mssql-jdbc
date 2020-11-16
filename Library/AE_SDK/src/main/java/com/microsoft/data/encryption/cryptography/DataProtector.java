package com.microsoft.data.encryption.cryptography;

abstract class DataProtector {
    abstract byte[] Decrypt(byte[] ciphertext) throws AAPSDKException;

    abstract byte[] Encrypt(byte[] plaintext) throws AAPSDKException;
}
