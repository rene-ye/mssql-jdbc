//package com.microsoft.data.encryption.cryptography;
//
//public class testEncryptNormal {
//    
//    // Column encryption key literal - for reproducable results.
//    public final static String decryptedKey = "0000010000000000030005000000400000000100000000000000000000000000";
//
//    
//    public static void main(String[] args) throws AAPSDKException {
//        // Get decrypted key, need to grab CMK and decrypt the CEK with the CMK. The decrypted key is a byte[32].
//        SqlServerSymmetricKey key = new SqlServerSymmetricKey(hexStringToByteArray(decryptedKey));
//        AeadAes256CbcHmac256Factory factory = new AeadAes256CbcHmac256Factory();
//        SQLServerEncryptionAlgorithm alg = factory.create(key, SqlServerEncryptionType.Deterministic);
//        // Need to turn the user's input into a byte[]
//        byte[] encrypted = alg.encryptData("Hello World".getBytes());
//        System.out.println(bytesToHex(encrypted));
//        byte[] unencrypted = alg.decryptData(encrypted);
//        System.out.println(new String(unencrypted));
//    }
//    
//    public static byte[] hexStringToByteArray(String s) {
//        int len = s.length();
//        byte[] data = new byte[len / 2];
//        for (int i = 0; i < len; i += 2) {
//            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
//                                 + Character.digit(s.charAt(i+1), 16));
//        }
//        return data;
//    }
//    
//    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
//    public static String bytesToHex(byte[] bytes) {
//        char[] hexChars = new char[bytes.length * 2];
//        for (int j = 0; j < bytes.length; j++) {
//            int v = bytes[j] & 0xFF;
//            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
//            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
//        }
//        return new String(hexChars);
//    }
//}
