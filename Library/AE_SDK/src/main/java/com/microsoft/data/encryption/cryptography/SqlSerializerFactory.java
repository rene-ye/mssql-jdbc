package com.microsoft.data.encryption.cryptography;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SqlSerializerFactory extends SerializerFactory {
    private static Map<String, ISerializer> serializerByIdentifier = new ConcurrentHashMap<String, ISerializer>();
    private static Map<Type, ISerializer> serializerByType = new ConcurrentHashMap<Type, ISerializer>();

    @Override
    public ISerializer getSerializer(String id) throws AAPSDKException {
        ISerializer s = serializerByIdentifier.get(id);
        if (null == s) {
            s = createSerializer(id, 0, 0, false);
        }
        return s;
    }

    public static ISerializer getOrCreate(String id, int precision, int scale) throws AAPSDKException {
        ISerializer s = serializerByType.get(new Type(id, precision, scale));
        if (null == s) {
            s = createSerializer(id, precision, scale, true);
        }
        return s;
    }

    private static ISerializer createSerializer(String id, int precision, int scale,
            boolean isByType) throws AAPSDKException {
        ISerializer s = null;
        switch (id.toLowerCase()) {
            case "bigint":
                s = new SqlBigIntSerializer(precision, scale);
                break;
            case "binary":
                s = new SqlBinarySerializer(precision, scale);
                break;
            case "bit":
                s = new SqlBooleanSerializer(precision, scale);
                break;
            case "char":
                s = new SqlCharSerializer(precision, scale);
                break;
            case "date":
                s = new SqlDateSerializer(precision, scale);
                break;
            case "datetime2":
                s = new SqlDateTime2Serializer(precision, scale);
                break;
            case "datetimeoffset":
                s = new SqlDateTimeOffsetSerializer(precision, scale);
                break;
            case "datetime":
                s = new SqlDateTimeSerializer(precision, scale);
                break;
            case "decimal":
                s = new SqlDecimalSerializer(precision, scale);
                break;
            case "float":
                s = new SqlFloatSerializer(precision, scale);
                break;
            case "int":
            case "integer":
                s = new SqlIntegerSerializer(precision, scale);
                break;
            case "money":
                s = new SqlMoneySerializer(precision, scale);
                break;
            case "nchar":
                s = new SqlNCharSerializer(precision, scale);
                break;
            case "nvarchar":
                s = new SqlNVarcharSerializer(precision, scale);
                break;
            case "real":
                s = new SqlRealSerializer(precision, scale);
                break;
            case "smalldatetime":
                s = new SqlSmalldatetimeSerializer(precision, scale);
                break;
            case "smallint":
                s = new SqlSmallintSerializer(precision, scale);
                break;
            case "smallmoney":
                s = new SqlSmallmoneySerializer(precision, scale);
                break;
            case "time":
                s = new SqlTimeSerializer(precision, scale);
                break;
            case "tinyint":
                s = new SqlTinyintSerializer(precision, scale);
                break;
            case "uniqueidentifier":
            case "guid":
                s = new SqlUniqueidentifierSerializer(precision, scale);
                break;
            case "varbinary":
                s = new SqlVarbinarySerializer(precision, scale);
                break;
            case "varchar":
                s = new SqlVarcharSerializer(precision, scale);
                break;
            default:
                throw new AAPSDKException(AAPSDKResource.getResource("R_InvalidSerializerName"));
        }
        if (isByType) {
            serializerByType.put(new Type(id, precision, scale), s);
        } else {
            serializerByIdentifier.put(id, s);
        }
        return s;
    }

    @Override
    public <T> ISerializer getDefaultSerializer(T o) throws AAPSDKException {
        return getSerializer(o.getClass().getName());
    }

    @Override
    public void registerSerializer(String typeName, ISerializer sqlSerializer, boolean overrideDefault) {
        serializerByIdentifier.put(typeName, sqlSerializer);
        // TODO: does java need to implement the overridesDefault part?
    }
}
