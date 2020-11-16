package com.microsoft.data.encryption.cryptography;

public class AAPSDKException extends Exception {

    /**
     * Creates the custom exception for AAP SDK.
     */
    public AAPSDKException() {
        super();
    }

    /**
     * Creates the custom exception for AAP SDK.
     * 
     * @param errMessage
     *        error message
     */
    public AAPSDKException(String errMessage) {
        super(errMessage);
    }

    static String getErrString(String errCode) {
        return AAPSDKResource.getResource(errCode);
    }
}
