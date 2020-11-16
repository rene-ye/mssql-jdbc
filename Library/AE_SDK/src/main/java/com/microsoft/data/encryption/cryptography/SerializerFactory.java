package com.microsoft.data.encryption.cryptography;

public abstract class SerializerFactory {

    public abstract ISerializer getSerializer(String identifier) throws AAPSDKException;

    public abstract <T> ISerializer getDefaultSerializer(T o) throws AAPSDKException;

    // TODO: Revisit.
    public abstract void registerSerializer(String typeName, ISerializer sqlSerializer, boolean overrideDefault);

}
