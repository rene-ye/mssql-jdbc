package com.microsoft.data.encryption.cryptography;

import java.util.Arrays;

import com.microsoft.data.encryption.cryptography.EncryptionJavaKeyStoreProvider;

public class testKeystore {
    
    private static String letters = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private static byte[] valuesDefault = letters.getBytes();
    private static String jksPaths1 = "3b82f8.jks";
    private static String keyStoreSecret = "changeit";
    private static String alias = "lp-e796bdea-c1df-4a27-b657-2bb71e2837d1";


    public static void main(String[] args) throws AAPSDKException {
        EncryptionJavaKeyStoreProvider storeProviderJava = new EncryptionJavaKeyStoreProvider(
                jksPaths1, keyStoreSecret.toCharArray());
        
        byte[] encryptedCEKValueJava = storeProviderJava.encryptEncryptionKey(alias, "RSA_OAEP", valuesDefault);
        
        byte[] decryptedEKValueJava = storeProviderJava.decryptEncryptionKey(alias, "RSA_OAEP", encryptedCEKValueJava);
        
        if(Arrays.equals(decryptedEKValueJava, valuesDefault)) {
            System.out.println("the arrays are equal");
        } else {
            System.out.println("the arrays are not equal");
        }
    }
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
