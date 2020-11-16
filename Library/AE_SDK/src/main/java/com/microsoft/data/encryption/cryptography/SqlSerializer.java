package com.microsoft.data.encryption.cryptography;

import java.math.BigDecimal;


abstract class SqlSerializer extends Serializer<Object> {
    int precision = 0;
    int scale = 0;

    public SqlSerializer(int precision, int scale) {
        this.precision = precision;
        this.scale = scale;
    }
}


class SqlBigIntSerializer extends SqlSerializer {

    public SqlBigIntSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.BIGINT, value, precision, scale);
        return null;
    }

    @Override
    public Long deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;

        return (Long) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.BIGINT, SSType.BIGINT, precision, scale,
                null);
    }
}


class SqlBinarySerializer extends SqlSerializer {

    public SqlBinarySerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.BINARY, value, precision, scale);
        return null;
    }

    @Override
    public byte[] deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;

        return (byte[]) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.BINARY, SSType.BINARY, precision, scale,
                null);
    }
}


class SqlBooleanSerializer extends SqlSerializer {

    public SqlBooleanSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.BIT, value, precision, scale);
        return null;
    }

    @Override
    public Boolean deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;

        return (Boolean) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.BIT, SSType.BIT, precision, scale, null);
    }
}


class SqlCharSerializer extends SqlSerializer {

    public SqlCharSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.CHAR, value, precision, scale);
        return null;
    }

    @Override
    public String deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (String) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.CHAR, SSType.CHAR, precision, scale,
                    null);
    }
}


class SqlDateSerializer extends SqlSerializer {

    public SqlDateSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.DATE, value, precision, scale);
        return null;
    }

    @Override
    public java.sql.Date deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (java.sql.Date) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.DATE, SSType.DATE, precision,
                    scale, null);
    }
}


class SqlDateTime2Serializer extends SqlSerializer {

    public SqlDateTime2Serializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.TIMESTAMP, value, precision, scale);
        return null;
    }

    @Override
    public java.sql.Timestamp deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (java.sql.Timestamp) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.TIMESTAMP, SSType.DATETIME2,
                    precision, scale, null);
    }
}


class SqlDateTimeOffsetSerializer extends SqlSerializer {

    public SqlDateTimeOffsetSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.DATETIMEOFFSET, value, precision, scale);
        return null;
    }

    @Override
    public DateTimeOffset deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (DateTimeOffset) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.DATETIMEOFFSET,
                    SSType.DATETIMEOFFSET, precision, scale, null);
    }
}


class SqlDateTimeSerializer extends SqlSerializer {

    public SqlDateTimeSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.DATETIME, value, precision, scale);
        return null;
    }

    @Override
    public java.sql.Timestamp deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (java.sql.Timestamp) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.DATETIME, SSType.DATETIME,
                    precision, scale, null);
    }
}


class SqlDecimalSerializer extends SqlSerializer {

    public SqlDecimalSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.DECIMAL, value, precision, scale);
        return null;
    }

    @Override
    public BigDecimal deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (BigDecimal) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.DECIMAL, SSType.DECIMAL, precision,
                    scale, null);
    }
}


class SqlFloatSerializer extends SqlSerializer {

    public SqlFloatSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.FLOAT, value, precision, scale);
        return null;
    }

    @Override
    public Double deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (Double) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.FLOAT, SSType.FLOAT, precision, scale,
                    null);
    }
}


class SqlIntegerSerializer extends SqlSerializer {

    public SqlIntegerSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.INTEGER, value, precision, scale);
        return null;
    }

    @Override
    public Integer deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (Integer) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.INTEGER, SSType.INTEGER, precision,
                    scale, null);
    }
}


class SqlMoneySerializer extends SqlSerializer {

    public SqlMoneySerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.MONEY, value, precision, scale);
        return null;
    }

    @Override
    public BigDecimal deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (BigDecimal) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.MONEY, SSType.MONEY, precision,
                    scale, null);
    }
}


class SqlNCharSerializer extends SqlSerializer {

    public SqlNCharSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.NCHAR, value, precision, scale);
        return null;
    }

    @Override
    public String deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (String) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.NCHAR, SSType.NCHAR, precision, scale,
                    null);
    }
}


class SqlNVarcharSerializer extends SqlSerializer {

    public SqlNVarcharSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.NVARCHAR, value, precision, scale);
        return null;
    }

    @Override
    public String deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (String) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.NVARCHAR, SSType.NVARCHAR, precision,
                    scale, null);
    }
}


class SqlRealSerializer extends SqlSerializer {

    public SqlRealSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.REAL, value, precision, scale);
        return null;
    }

    @Override
    public Float deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (Float) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.REAL, SSType.REAL, precision, scale,
                    null);
    }
}


class SqlSmalldatetimeSerializer extends SqlSerializer {

    public SqlSmalldatetimeSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.SMALLDATETIME, value, precision, scale);
        return null;
    }

    @Override
    public java.sql.Timestamp deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (java.sql.Timestamp) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.SMALLDATETIME,
                    SSType.SMALLDATETIME, precision, scale, null);
    }
}


class SqlSmallintSerializer extends SqlSerializer {

    public SqlSmallintSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.SMALLINT, value, precision, scale);
        return null;
    }

    @Override
    public Short deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (Short) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.SMALLINT, SSType.SMALLINT, precision,
                    scale, null);
    }
}


class SqlSmallmoneySerializer extends SqlSerializer {

    public SqlSmallmoneySerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.SMALLMONEY, value, precision, scale);
        return null;
    }

    @Override
    public BigDecimal deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (BigDecimal) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.SMALLMONEY, SSType.SMALLMONEY,
                    precision, scale, null);
    }
}


class SqlTimeSerializer extends SqlSerializer {

    public SqlTimeSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null) {
            if (value instanceof java.sql.Time) {
                String time = "1900-01-01 " + value.toString();
                java.sql.Timestamp ts = java.sql.Timestamp.valueOf(time);
                return SqlSerializerUtil.normalizedValue(JDBCType.TIME, ts, precision, scale);
            } else {
                return SqlSerializerUtil.normalizedValue(JDBCType.TIME, value, precision, scale);

            }
        }
        return null;
    }

    @Override
    public java.sql.Time deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (java.sql.Time) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.TIME, SSType.TIME, precision,
                    scale, null);
    }
}


class SqlTinyintSerializer extends SqlSerializer {

    public SqlTinyintSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.TINYINT, value, precision, scale);
        return null;
    }

    @Override
    public Short deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (Short) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.TINYINT, SSType.TINYINT, precision,
                    scale, null);
    }
}


class SqlUniqueidentifierSerializer extends SqlSerializer {

    public SqlUniqueidentifierSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.GUID, value, precision, scale);
        return null;
    }

    @Override
    public String deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (String) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.GUID, SSType.GUID, precision, scale,
                    null);
    }
}


class SqlVarbinarySerializer extends SqlSerializer {

    public SqlVarbinarySerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.VARBINARY, value, precision, scale);
        return null;
    }

    @Override
    public byte[] deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (byte[]) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.VARBINARY, SSType.VARBINARY, precision,
                    scale, null);
    }
}


class SqlVarcharSerializer extends SqlSerializer {

    public SqlVarcharSerializer(int precision, int scale) {
        super(precision, scale);
    }

    @Override
    public byte[] serialize(Object value) throws AAPSDKException {
        if (value != null)
            return SqlSerializerUtil.normalizedValue(JDBCType.VARCHAR, value, precision, scale);
        return null;
    }

    @Override
    public String deserialize(byte[] bytes) throws AAPSDKException {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return (String) SqlSerializerUtil.denormalizedValue(bytes, JDBCType.VARCHAR, SSType.VARCHAR, precision,
                    scale, null);
    }
}
