package com.microsoft.data.encryption.cryptography;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class StandardSerializerFactory extends SerializerFactory {

    private Map<String, ISerializer> serializers = new ConcurrentHashMap<>();

    // TODO: explain, for this and SQLSerializer, why we return ISerailizer instead of Serializer<T>.
    // mostly has to do with the difference between C# and java.
    @Override
    public ISerializer getSerializer(String id) {
        ISerializer s = serializers.get(id);
        if (null == s) {
            s = createSerializer(id);
        }
        return s;
    }

    @Override
    public <T> Serializer<T> getDefaultSerializer(T o) {
        return (Serializer<T>) getSerializer(o.getClass().getName());
    }

    public <T> Serializer<T> getDefaultSerializer(Class<T> o) {
        return (Serializer<T>) getSerializer(o.getName());
    }

    @Override
    public void registerSerializer(String typeName, ISerializer s, boolean overrideDefault) {
        serializers.put(typeName, s);
        // TODO: does java need to implement the overridesDefault part?
    }

    private Serializer<?> createSerializer(String id) {
        Serializer<?> s = null;
        switch (id) {
            case "java.lang.Boolean":
                s = new BooleanSerializer();
                break;
            case "java.lang.Integer":
                s = new IntegerSerializer();
                break;
            case "java.lang.Byte":
                s = new ByteSerializer();
                break;
            case "java.lang.Double":
                s = new DoubleSerializer();
                break;
            case "java.lang.Float":
                s = new FloatSerializer();
                break;
            case "java.lang.String":
                s = new StringSerializer();
                break;
            case "java.lang.Character":
                s = new CharSerializer();
                break;
            case "java.util.UUID":
                s = new UuidSerializer();
                break;
            default:
                return null;
        }
        if (null != s)
            serializers.put(id, s);
        return s;
    }
}
