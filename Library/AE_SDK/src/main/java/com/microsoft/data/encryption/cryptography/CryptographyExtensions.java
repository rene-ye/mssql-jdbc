package com.microsoft.data.encryption.cryptography;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


/**
 * Utility class for cryptography operations.
 *
 */
public final class CryptographyExtensions {

    private final static char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
            'F'};
    private static final StandardSerializerFactory STANDARD_SERIALIZER_FACTORY = new StandardSerializerFactory();

    private CryptographyExtensions() {}

    /**
     * Encrypts given plaintext according to the specified type, using the provided encryption key.
     * 
     * @param plaintext
     *        plaintext data
     * @param encryptionKey
     *        encryption key to be used
     * @param typeParameter
     *        type of serializer to be used
     * @return encrpyted byte array
     * @throws AAPSDKException
     */
    public static <T> byte[] Encrypt(T plaintext, DataEncryptionKey encryptionKey,
            Class<T> typeParameter) throws AAPSDKException {
        if (null == encryptionKey) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_NullColumnEncryptionKey"));
        }

        DataProtector encryptionAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm.getOrCreate(encryptionKey,
                EncryptionType.Randomized);
        Serializer<T> serializer = (Serializer<T>) STANDARD_SERIALIZER_FACTORY.getDefaultSerializer(typeParameter);
        byte[] serializedData = serializer.serialize(plaintext);
        return encryptionAlgorithm.Encrypt(serializedData);
    }

    /**
     * Decrypts given ciphertext according to the specified type, using the provided encryption key.
     * 
     * @param ciphertext
     *        encrypted data
     * @param encryptionKey
     *        encryption key to be used
     * @param typeParameter
     *        type of serializer to be used
     * @return plaintext data
     * @throws AAPSDKException
     */
    public static <T> T Decrypt(byte[] ciphertext, DataEncryptionKey encryptionKey,
            Class<T> typeParameter) throws AAPSDKException {
        if (null == encryptionKey) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_NullColumnEncryptionKey"));
        }

        DataProtector encryptionAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm.getOrCreate(encryptionKey,
                EncryptionType.Randomized);
        Serializer<T> serializer = STANDARD_SERIALIZER_FACTORY.getDefaultSerializer(typeParameter);
        byte[] plaintextData = encryptionAlgorithm.Decrypt(ciphertext);
        return serializer.deserialize(plaintextData);
    }

    /**
     * Encrypts given plaintext according to the specified type.
     * 
     * @param plaintext
     *        plaintext data
     * @param encryptionSettings
     *        encryption settings
     * @return encrpyted byte array
     * @throws AAPSDKException
     */
    public static <T> byte[] Encrypt(T plaintext, EncryptionSettingsImpl encryptionSettings) throws AAPSDKException {
        if (null == encryptionSettings) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_NullEncryptionSettings"));
        }

        if (EncryptionType.Plaintext == encryptionSettings.getEncryptionType()) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_PlaintextEncryptionSettings"));
        }

        DataProtector encryptionAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm
                .getOrCreate(encryptionSettings.getDataEncryptionKey(), encryptionSettings.getEncryptionType());
        Serializer<T> serializer = (Serializer<T>) encryptionSettings.getSerializer();
        byte[] serializedData = serializer.serialize(plaintext);
        return encryptionAlgorithm.Encrypt(serializedData);
    }

    /**
     * Decrypts given ciphertext according to the specified type.
     * 
     * @param ciphertext
     *        encrypted data
     * @param encryptionSettings
     *        encryption settings
     * @return plaintext data
     * @throws AAPSDKException
     */
    public static <T> T Decrypt(byte[] ciphertext, EncryptionSettingsImpl encryptionSettings) throws AAPSDKException {
        if (null == encryptionSettings) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_NullEncryptionSettings"));
        }

        if (EncryptionType.Plaintext == encryptionSettings.getEncryptionType()) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_PlaintextEncryptionSettings"));
        }

        DataProtector encryptionAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm
                .getOrCreate(encryptionSettings.getDataEncryptionKey(), EncryptionType.Plaintext);
        Serializer<T> serializer = (Serializer<T>) encryptionSettings.getSerializer();
        byte[] plaintextData = encryptionAlgorithm.Decrypt(ciphertext);
        return serializer.deserialize(plaintextData);
    }

    /**
     * Encrypts given plaintext according to the specified type.
     * 
     * @param source
     *        iterable plaintext data source
     * @param encryptionKey
     *        encryption key to be used
     * @param typeParameter
     *        type of serializer to be used
     * @return Iterable containing encrpyted byte array
     * @throws AAPSDKException
     */
    public static <T> Iterable<byte[]> Encrypt(Iterable<T> source, DataEncryptionKey encryptionKey,
            Class<T> typeParameter) throws AAPSDKException {
        if (null == encryptionKey) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_NullColumnEncryptionKey"));
        }

        DataProtector encryptionAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm.getOrCreate(encryptionKey,
                EncryptionType.Randomized);
        Serializer<T> serializer = STANDARD_SERIALIZER_FACTORY.getDefaultSerializer(typeParameter);

        List<byte[]> r = new ArrayList<byte[]>();

        for (T item : source) {
            byte[] serializedData = serializer.serialize(item);
            r.add(encryptionAlgorithm.Encrypt(serializedData));
        }

        return r;
    }

    /**
     * Decrypts given ciphertext according to the specified type.
     * 
     * @param source
     *        iterable ciphertext data source
     * @param encryptionKey
     *        encryption key to be used
     * @param typeParameter
     *        type of serializer to be used
     * @return Iterable containing decrypted data
     * @throws AAPSDKException
     */
    public static <T> Iterable<T> Decrypt(Iterable<byte[]> source, DataEncryptionKey encryptionKey,
            Class<T> typeParameter) throws AAPSDKException {
        if (null == encryptionKey) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_NullColumnEncryptionKey"));
        }

        DataProtector encryptionAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm.getOrCreate(encryptionKey,
                EncryptionType.Randomized);
        Serializer<T> serializer = STANDARD_SERIALIZER_FACTORY.getDefaultSerializer(typeParameter);

        List<T> r = new ArrayList<T>();

        for (byte[] item : source) {
            byte[] plaintextData = encryptionAlgorithm.Decrypt(item);
            r.add(serializer.deserialize(plaintextData));
        }

        return r;
    }

    /**
     * Encrypts given object according to the specified type.
     * 
     * @param source
     *        iterable data source
     * @param encryptionSettings
     *        encryption settings
     * @return Iterable containing encrpyted byte array
     * @throws AAPSDKException
     */
    public static <T> Iterable<byte[]> Encrypt(Iterable<T> source,
            EncryptionSettingsImpl encryptionSettings) throws AAPSDKException {
        if (null == encryptionSettings) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_NullEncryptionSettings"));
        }

        if (EncryptionType.Plaintext == encryptionSettings.getEncryptionType()) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_PlaintextEncryptionSettings"));
        }

        DataProtector encryptionAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm
                .getOrCreate(encryptionSettings.getDataEncryptionKey(), encryptionSettings.getEncryptionType());
        Serializer<T> serializer = (Serializer<T>) encryptionSettings.getSerializer();

        List<byte[]> r = new ArrayList<byte[]>();

        for (T item : source) {
            byte[] serializedData = serializer.serialize(item);
            r.add(encryptionAlgorithm.Encrypt(serializedData));
        }

        return r;
    }

    /**
     * Decrypts given ciphertext according to the specified type.
     * 
     * @param source
     *        iterable ciphertext data source
     * @param encryptionSettings
     *        encryption settings
     * @return Iterable containing decrypted data
     * @throws AAPSDKException
     */
    public static <T> Iterable<T> Decrypt(Iterable<byte[]> source,
            EncryptionSettingsImpl encryptionSettings) throws AAPSDKException {
        if (null == encryptionSettings) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_NullEncryptionSettings"));
        }

        if (EncryptionType.Plaintext == encryptionSettings.getEncryptionType()) {
            throw new AAPSDKException(AAPSDKResource.getResource("R_PlaintextEncryptionSettings"));
        }

        DataProtector encryptionAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm
                .getOrCreate(encryptionSettings.getDataEncryptionKey(), EncryptionType.Plaintext);
        Serializer<T> serializer = (Serializer<T>) encryptionSettings.getSerializer();

        List<T> r = new ArrayList<T>();

        for (byte[] item : source) {
            byte[] plaintextData = encryptionAlgorithm.Decrypt(item);
            r.add(serializer.deserialize(plaintextData));
        }

        return r;
    }

    /**
     * Converts an array bytes to its equivalent string representation that is encoded with base-64 digits.
     * 
     * @param source
     *        An array of bytes.
     * @return The string representation, in base 64, of the contents of source.
     */
    public static String ToBase64String(byte[] source) {
        byte[] encoded = Base64.getEncoder().encode(source);
        return new String(encoded);
    }

    /**
     * Converts the specified string, which encodes binary data as base-64 digits, to an equivalent byte array.
     * 
     * @param source
     *        The string to convert.
     * @return An array of bytes that is equivalent to source.
     */
    public static byte[] FromBase64String(String source) {
        return Base64.getDecoder().decode(source);
    }

    /**
     * Converts each byte array in the source sequence to its equivalent string representation that is encoded with
     * base-64 digits.
     * 
     * @param source
     *        A sequence of byte arrays to convert.
     * @return An Iterable of String whose elements are the result of being encoded with base-64 digits.
     */
    public static Iterable<String> ToBase64String(Iterable<byte[]> source) {
        List<String> r = new ArrayList<String>();
        for (byte[] item : source) {
            r.add(ToBase64String(item));
        }
        return r;
    }

    /**
     * Converts each String element of source, which encodes binary data as base-64 digits, to an equivalent byte array.
     * 
     * @param source
     *        A sequence of strings to convert.
     * @return An Iterable of byte arrays that is equivalent to source.
     */
    public static Iterable<byte[]> FromBase64String(Iterable<String> source) {
        List<byte[]> r = new ArrayList<byte[]>();
        for (String item : source) {
            r.add(FromBase64String(item));
        }

        return r;
    }

    /**
     * Converts the numeric value of each element of a specified array of bytes to its equivalent hexadecimal string
     * representation.
     * 
     * @param source
     *        An array of bytes to convert.
     * @return A string of hexadecimal characters
     */
    public static String ToHexString(byte[] source) {
        if (null == source) {
            return null;
        }

        StringBuilder sb = new StringBuilder(source.length * 2);
        for (int i = 0; i < source.length; i++) {
            int hexVal = source[i] & 0xFF;
            sb.append(hexChars[(hexVal & 0xF0) >> 4]);
            sb.append(hexChars[(hexVal & 0x0F)]);
        }
        return sb.toString();
    }

    /**
     * Converts the string representation of a number in hexidecimal to an equivalent array of bytes.
     * 
     * @param source
     *        The string to convert.
     * @return >An array of bytes that is equivalent to source.
     */
    public static byte[] FromHexString(String source) {
        if (null == source) {
            return null;
        }

        int len = source.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(source.charAt(i), 16) << 4)
                    + Character.digit(source.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Converts each byte array in the source sequence to its equivalent string representation that is encoded with
     * hexidecimal digits.
     * 
     * @param source
     *        A sequence of byte arrays to convert.
     * @return An Iterable of String whose elements are the result of being encoded with hexidecimal digits.
     */
    public static Iterable<String> ToHexString(Iterable<byte[]> source) {
        List<String> r = new ArrayList<String>();
        for (byte[] item : source) {
            r.add(ToHexString(item));
        }
        return r;
    }

    /**
     * Converts each String element of source, which encodes binary data as hexidecimal digits, to an equivalent byte
     * array.
     * 
     * @param source
     *        A sequence of strings to convert.
     * @return An Iterable of byte arrays that is equivalent to source.
     */
    public static Iterable<byte[]> FromHexString(Iterable<String> source) {
        List<byte[]> r = new ArrayList<byte[]>();
        for (String item : source) {
            r.add(FromHexString(item));
        }

        return r;
    }
}
