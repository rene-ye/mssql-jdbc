package com.microsoft.data.encryption.cryptography;

import static org.junit.Assert.fail;

import java.net.URI;


/**
 * Generic Utility class which we can access by test classes.
 * 
 */
public final class TestUtils {

    private static final char[] HEXCHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
            'F'};

    /**
     * 
     * @return location of resource file
     */
    public static String getCurrentClassPath() {
        try {
            String className = new Object() {}.getClass().getEnclosingClass().getName();
            String location = Class.forName(className).getProtectionDomain().getCodeSource().getLocation().getPath();
            URI uri = new URI(location + "/");
            return uri.getPath();
        } catch (Exception e) {
            fail("Failed to get CSV file path. " + e.getMessage());
        }
        return null;
    }

    /**
     * 
     * @param b
     *        byte value
     * @param length
     *        length of the array
     * @return
     */
    public static String bytesToHexString(byte[] b, int length) {
        StringBuilder sb = new StringBuilder(length * 2);
        for (int i = 0; i < length; i++) {
            int hexVal = b[i] & 0xFF;
            sb.append(HEXCHARS[(hexVal & 0xF0) >> 4]);
            sb.append(HEXCHARS[(hexVal & 0x0F)]);
        }
        return sb.toString();
    }
}
