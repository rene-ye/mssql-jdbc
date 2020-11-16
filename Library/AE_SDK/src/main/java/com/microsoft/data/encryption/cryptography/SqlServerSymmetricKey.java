package com.microsoft.data.encryption.cryptography;

import java.text.MessageFormat;


/**
 * 
 * Base class which represents Symmetric key
 *
 */
public class SqlServerSymmetricKey {
    private byte[] rootKey;

    public SqlServerSymmetricKey(byte[] rootKey) throws AAPSDKException {
        if (null == rootKey || 0 == rootKey.length) {
            MessageFormat form = new MessageFormat(AAPSDKResource.getResource("R_CannotBeNullOrWhiteSpace"));
            Object[] msgArgs = {"rooykey"};
            throw new AAPSDKException(form.format(msgArgs));
        }
        this.rootKey = rootKey;
    }

    byte[] getRootKey() {
        return rootKey;
    }

    int length() {
        return rootKey.length;
    }

    void zeroOutKey() {
        for (int i = 0; i < rootKey.length; i++) {
            rootKey[i] = (byte) 0;
        }
    }
}
