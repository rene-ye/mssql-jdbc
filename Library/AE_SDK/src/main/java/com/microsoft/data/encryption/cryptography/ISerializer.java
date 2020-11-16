package com.microsoft.data.encryption.cryptography;

public interface ISerializer {
    byte[] serialize(Object value) throws AAPSDKException;

    Object deserialize(byte[] bytes) throws AAPSDKException;
}


abstract class Serializer<T> implements ISerializer {
    public String identifier;

    public String getIdentifier() {
        return identifier;
    }

    public abstract byte[] serialize(Object value) throws AAPSDKException;

    public abstract T deserialize(byte[] bytes) throws AAPSDKException;
}
