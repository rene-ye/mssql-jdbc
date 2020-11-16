package com.microsoft.data.encryption.cryptography;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement;
import com.microsoft.sqlserver.jdbc.SQLServerResultSet;


@RunWith(JUnitPlatform.class)
public class testSQLSerializer extends AESetup {

    private static String randomTableName1 = RandomUtil
            .escapeIdentifier(RandomUtil.getIdentifier("SQLSerializerTable_deterministic"));
    private static String randomTableName2 = RandomUtil
            .escapeIdentifier(RandomUtil.getIdentifier("SQLSerializerTable_randomized"));

    @Test
    public void testBigInteger() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.BIGINT.name(), 0, 0);
        Long[] dataArray = {Long.MAX_VALUE, 9876543210L, 1L, 0L, -1L, -9876543210L, Long.MIN_VALUE};
        for (Long data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            Long deserializedData = (Long) serializer.deserialize(serializedData);
            assertEquals(data, deserializedData);
        }
    }

    @Test
    public void testBigIntegerWithDriver() throws AAPSDKException {
        String type = "bigint";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.BIGINT.name(), 0, 0);
        Long[] dataArray = {Long.MAX_VALUE, 9876543210L, 1L, 0L, -1L, -9876543210L, Long.MIN_VALUE};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (Long data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                Long deserializedData = (Long) serializer.deserialize(serializedData);
                assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                // test decrypted data
                assertEquals(deserializedData,
                        (Long) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (Long data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                assertEquals((Long) rsAE.getLong(1),
                        (Long) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testBinary() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.BINARY.name(), 5, 0);
        byte[][] dataArray = {null, new byte[] {0, 0, 0, 0, 0}, new byte[] {127, 127, 127, 127, 127},
                new byte[] {-128, -128, -128, -128, -128}};
        for (byte[] data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            byte[] deserializedData = (byte[]) serializer.deserialize(serializedData);
            assertTrue(Arrays.equals(data, deserializedData));
        }
    }

    @Test
    public void testBinaryWithDriver() throws AAPSDKException {
        String type = "binary(5)";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.BINARY.name(), 5, 0);
        byte[][] dataArray = {null, new byte[] {0, 0, 0, 0, 0}, new byte[] {127, 127, 127, 127, 127},
                new byte[] {-128, -128, -128, -128, -128}};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (byte[] data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                byte[] deserializedData = (byte[]) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertTrue(Arrays.equals(deserializedData,
                            (byte[]) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1)))));
                }
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (byte[] data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data && null == rsAE.getBytes(1)) {
                    // pass
                } else {
                    assertTrue(Arrays.equals((byte[]) rsAE.getBytes(1),
                            (byte[]) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1)))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testBoolean() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.BIT.name(), 0, 0);
        Boolean[] dataArray = {false, true};
        for (Boolean data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            boolean deserializedData = (boolean) serializer.deserialize(serializedData);
            assertEquals(data, deserializedData);
        }
    }

    @Test
    public void testBooleanWithDriver() throws AAPSDKException {
        String type = "bit";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.BIT.name(), 0, 0);
        Boolean[] dataArray = {false, true};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (Boolean data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                Boolean deserializedData = (Boolean) serializer.deserialize(serializedData);
                assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                // test decrypted data
                assertEquals(deserializedData,
                        (Boolean) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (Boolean data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                assertEquals((Boolean) rsAE.getBoolean(1),
                        (Boolean) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testCharacter() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.CHAR.name(), 25, 0);
        String[] dataArray = {"a", null, "말", "0000000000123456789", ""};
        String[] expectedArray = {"a                        ", null, "말                        ",
                "0000000000123456789      ", null};
        int count = 0;
        for (String data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            String deserializedData = (String) serializer.deserialize(serializedData);
            assertEquals(expectedArray[count], deserializedData);
            count++;
        }
    }

    @Test
    public void testCharacterWithDriver() throws AAPSDKException {
        String type = "char(25)";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.CHAR.name(), 25, 0);
        String[] dataArray = {"a", null, "0000000000123456789", ""};
        String[] expectedArray = {"a                        ", null, "0000000000123456789      ", null};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            int count = 0;
            for (String data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                String deserializedData = (String) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(expectedArray[count],
                            (String) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
                }
                count++;
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (String data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data || data.isEmpty()) {
                    // pass
                } else {
                    assertEquals((String) rsAE.getString(1),
                            (String) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testDate() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.DATE.name(), 0, 0);
        java.sql.Date[] dataArray = {null, new java.sql.Date(0L), new java.sql.Date(1600991197217L),
                new java.sql.Date(9999999999999L)};
        for (java.sql.Date data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            java.sql.Date deserializedData = (java.sql.Date) serializer.deserialize(serializedData);
            if (null == data && null == deserializedData) {
                // pass
            } else {
                assertEquals(data.toString(), deserializedData.toString());
            }
        }
    }

    @Test
    public void testDateWithDriver() throws AAPSDKException {
        String type = "date";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.DATE.name(), 0, 0);
        java.sql.Date[] dataArray = {null, new java.sql.Date(0L), new java.sql.Date(1600991197217L),
                new java.sql.Date(9999999999999L)};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (java.sql.Date data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                java.sql.Date deserializedData = (java.sql.Date) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(deserializedData,
                            (java.sql.Date) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (java.sql.Date data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data) {
                    // pass
                } else {
                    assertEquals((java.sql.Date) rsAE.getDate(1), (java.sql.Date) serializer
                            .deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testDateTime2() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.DATETIME2.name(), 0, 3);
        java.sql.Timestamp[] dataArray = {null, new java.sql.Timestamp(0L), new java.sql.Timestamp(1600991197216L),
                new java.sql.Timestamp(9999999999999L),};
        for (java.sql.Timestamp data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            java.sql.Timestamp deserializedData = (java.sql.Timestamp) serializer.deserialize(serializedData);
            assertEquals(data, deserializedData);
        }
    }

    @Test
    public void testDateTime2WithDriver() throws AAPSDKException {
        String type = "datetime2";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.DATETIME2.name(), 0, 3);
        java.sql.Timestamp[] dataArray = {null, new java.sql.Timestamp(0L), new java.sql.Timestamp(1600991197216L),
                new java.sql.Timestamp(9999999999999L),};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (java.sql.Timestamp data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                java.sql.Timestamp deserializedData = (java.sql.Timestamp) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(deserializedData, (java.sql.Timestamp) serializer
                            .deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (java.sql.Timestamp data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data) {
                    // pass
                } else {
                    assertEquals((java.sql.Timestamp) rsAE.getDateTime(1), (java.sql.Timestamp) serializer
                            .deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testDateTimeOffset() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.DATETIMEOFFSET.name(), 0, 3);
        DateTimeOffset[] dataArray = {null, DateTimeOffset.valueOf(new java.sql.Timestamp(0L), 0),
                DateTimeOffset.valueOf(new java.sql.Timestamp(1600991197216L), 0),
                DateTimeOffset.valueOf(new java.sql.Timestamp(9999999999999L), 0),};
        for (DateTimeOffset data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            DateTimeOffset deserializedData = (DateTimeOffset) serializer.deserialize(serializedData);
            assertEquals(data, deserializedData);
        }
    }

    @Test
    public void testDateTimeOffsetWithDriver() throws AAPSDKException {
        String type = "datetimeoffset";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.DATETIMEOFFSET.name(), 0, 3);
        DateTimeOffset[] dataArray = {DateTimeOffset.valueOf(new java.sql.Timestamp(0L), 0),
                DateTimeOffset.valueOf(new java.sql.Timestamp(1600991197216L), 0),
                DateTimeOffset.valueOf(new java.sql.Timestamp(9999999999999L), 0),};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (DateTimeOffset data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                DateTimeOffset deserializedData = (DateTimeOffset) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(deserializedData,
                            (DateTimeOffset) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (DateTimeOffset data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data) {
                    // pass
                } else {
                    assertEquals(DateTimeOffset.valueOf(rsAE.getTimestamp(1), 0), (DateTimeOffset) serializer
                            .deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testDateTime() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.DATETIME.name(), 0, 0);
        java.sql.Timestamp[] dataArray = {null, new java.sql.Timestamp(0L), new java.sql.Timestamp(1600991197217L),
                new java.sql.Timestamp(9999999999990L),};
        for (java.sql.Timestamp data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            java.sql.Timestamp deserializedData = (java.sql.Timestamp) serializer.deserialize(serializedData);
            assertEquals(data, deserializedData);
        }
    }

    @Test
    public void testDateTimeWithDriver() throws AAPSDKException {
        String type = "datetime";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.DATETIME.name(), 0, 0);
        java.sql.Timestamp[] dataArray = {null, new java.sql.Timestamp(0L), new java.sql.Timestamp(1600991197217L),
                new java.sql.Timestamp(9999999999990L),};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (java.sql.Timestamp data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                java.sql.Timestamp deserializedData = (java.sql.Timestamp) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(deserializedData, (java.sql.Timestamp) serializer
                            .deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (java.sql.Timestamp data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data) {
                    // pass
                } else {
                    assertEquals((java.sql.Timestamp) rsAE.getDateTime(1), (java.sql.Timestamp) serializer
                            .deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testDecimal() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.DECIMAL.name(), 19, 4);
        BigDecimal[] dataArray = {null, new BigDecimal(Long.MIN_VALUE), new BigDecimal(Long.MAX_VALUE),
                new BigDecimal(0L), new BigDecimal(1000000000L), new BigDecimal(-1000000000L),};
        for (BigDecimal data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            BigDecimal deserializedData = (BigDecimal) serializer.deserialize(serializedData);
            if (null == data && null == deserializedData) {
                // pass
            } else {
                assertEquals(data.stripTrailingZeros(), deserializedData.stripTrailingZeros());
            }
        }
    }

    @Test
    public void testDecimalWithDriver() throws AAPSDKException {
        String type = "decimal(38, 4)";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.DECIMAL.name(), 38, 4);
        BigDecimal[] dataArray = {new BigDecimal(Long.MIN_VALUE), new BigDecimal(Long.MAX_VALUE), new BigDecimal(0L),
                new BigDecimal(1000000000L), new BigDecimal(-1000000000L),};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (BigDecimal data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                BigDecimal deserializedData = (BigDecimal) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(deserializedData,
                            (BigDecimal) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (BigDecimal data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data) {
                    // pass
                } else {
                    assertEquals((BigDecimal) rsAE.getBigDecimal(1),
                            (BigDecimal) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testFloat() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.FLOAT.name(), 0, 0);
        Double[] dataArray = {null, 1234d, -1234d, 0d, Double.MIN_VALUE, Double.MAX_VALUE};
        for (Double data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            Double deserializedData = (Double) serializer.deserialize(serializedData);
            assertEquals(data, deserializedData);
        }
    }

    @Test
    public void testFloatWithDriver() throws AAPSDKException {
        String type = "float";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.FLOAT.name(), 0, 0);
        Double[] dataArray = {1234d, -1234d, 0d, Double.MIN_VALUE, Double.MAX_VALUE};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (Double data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                Double deserializedData = (Double) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(deserializedData,
                            (Double) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (Double data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data) {
                    // pass
                } else {
                    assertEquals((Double) rsAE.getDouble(1),
                            (Double) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testInteger() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.INTEGER.name(), 0, 0);
        Integer[] dataArray = {null, 1234, -1234, 0, Integer.MIN_VALUE, Integer.MAX_VALUE};
        for (Integer data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            Integer deserializedData = (Integer) serializer.deserialize(serializedData);
            assertEquals(data, deserializedData);
        }
    }

    @Test
    public void testIntegerWithDriver() throws AAPSDKException {
        String type = "integer";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.INTEGER.name(), 0, 0);
        Integer[] dataArray = {null, 1234, -1234, 0, Integer.MIN_VALUE, Integer.MAX_VALUE};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (Integer data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                Integer deserializedData = (Integer) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(deserializedData,
                            (Integer) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (Integer data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data) {
                    // pass
                } else {
                    assertEquals((Integer) rsAE.getInt(1),
                            (Integer) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testMoney() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.MONEY.name(), 32, 0);
        BigDecimal[] dataArray = {null, new BigDecimal(0L), new BigDecimal(1000000000L), new BigDecimal(-1000000000L)};
        for (BigDecimal data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            BigDecimal deserializedData = (BigDecimal) serializer.deserialize(serializedData);
            if (null == data && null == deserializedData) {
                // pass
            } else {
                assertEquals(data.stripTrailingZeros(), deserializedData.stripTrailingZeros());
            }
        }
    }
    
    @Test
    public void testMoneyWithDriver() throws AAPSDKException {
        String type = "money";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.MONEY.name(), 32, 0);
        BigDecimal[] dataArray = {new BigDecimal(0L), new BigDecimal(1000000000L), new BigDecimal(-1000000000L)};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (BigDecimal data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                BigDecimal deserializedData = (BigDecimal) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(deserializedData,
                            (BigDecimal) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (BigDecimal data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data) {
                    // pass
                } else {
                    assertEquals((BigDecimal) rsAE.getMoney(1),
                            (BigDecimal) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testNChar() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.NCHAR.name(), 10, 0);
        String[] dataArray = {"a", null, "말", "23456789", "ナンセンス", ""};
        String[] expectedArray = {"a         ", null, "말         ", "23456789  ", "ナンセンス     ", null};
        int count = 0;
        for (String data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            String deserializedData = (String) serializer.deserialize(serializedData);
            assertEquals(expectedArray[count], deserializedData);
            count++;
        }
    }
    
    @Test
    public void testNCharWithDriver() throws AAPSDKException {
        String type = "nchar(10)";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.NCHAR.name(), 10, 0);
        String[] dataArray = {"a", null, "말", "23456789", "ナンセンス", ""};
        String[] expectedArray = {"a         ", null, "말         ", "23456789  ", "ナンセンス     ", null};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            int count = 0;
            for (String data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                String deserializedData = (String) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(expectedArray[count],
                            (String) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
                }
                count++;
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (String data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data || data.isEmpty()) {
                    // pass
                } else {
                    assertEquals((String) rsAE.getNString(1),
                            (String) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testNVarchar() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.NVARCHAR.name(), 40, 0);
        String[] dataArray = {null, new String("Test NVarchar data"), new String("             "), new String("ナンセンス")};
        for (String data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            String deserializedData = (String) serializer.deserialize(serializedData);
            assertEquals(data, deserializedData);
        }
    }
    
    @Test
    public void testNVarcharWithDriver() throws AAPSDKException {
        String type = "nvarchar(40)";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.NVARCHAR.name(), 40, 0);
        String[] dataArray = {null, new String("Test NVarchar data"), new String("             "), new String("ナンセンス")};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (String data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                String deserializedData = (String) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(deserializedData,
                            (String) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (String data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data) {
                    // pass
                } else {
                    assertEquals((String) rsAE.getNString(1),
                            (String) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testReal() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.REAL.name(), 0, 0);
        Float[] dataArray = {null, 1234f, -1234f, 0f, Float.MIN_VALUE, Float.MAX_VALUE};
        for (Float data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            Float deserializedData = (Float) serializer.deserialize(serializedData);
            assertEquals(data, deserializedData);
        }
    }
    
    @Test
    public void testRealWithDriver() throws AAPSDKException {
        String type = "real";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.REAL.name(), 0, 0);
        Float[] dataArray = {1234f, -1234f, 0f, Float.MIN_VALUE, Float.MAX_VALUE};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (Float data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                Float deserializedData = (Float) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(deserializedData,
                            (Float) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (Float data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data) {
                    // pass
                } else {
                    assertEquals((Float) rsAE.getFloat(1),
                            (Float) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testSmalldatetime() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.SMALLDATETIME.name(), 0, 0);
        java.sql.Timestamp[] dataArray = {null, new java.sql.Timestamp(0L), new java.sql.Timestamp(1600991197216L)};
        String[] expectedData = {null, "1969-12-31 16:00:00.0", "2020-09-24 16:47:00.0"};
        int count = 0;
        for (java.sql.Timestamp data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            java.sql.Timestamp deserializedData = (java.sql.Timestamp) serializer.deserialize(serializedData);
            if (null == data && null == deserializedData) {
                // pass
            } else {
                assertEquals(expectedData[count], deserializedData.toString());
            }
            count++;
        }
    }
    
    @Test
    public void testSmalldatetimeWithDriver() throws AAPSDKException {
        String type = "smalldatetime";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.SMALLDATETIME.name(), 0, 0);
        java.sql.Timestamp[] dataArray = {null, new java.sql.Timestamp(0L), new java.sql.Timestamp(1600991197216L)};
        String[] expectedArray = {null, "1969-12-31 16:00:00.0", "2020-09-24 16:47:00.0"};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            int count = 0;
            for (java.sql.Timestamp data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                java.sql.Timestamp deserializedData = (java.sql.Timestamp) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(expectedArray[count],
                            ((java.sql.Timestamp) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1)))).toString());
                }
                count++;
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (java.sql.Timestamp data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data) {
                    // pass
                } else {
                    assertEquals((java.sql.Timestamp) rsAE.getTimestamp(1),
                            (java.sql.Timestamp) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testSmallInt() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.SMALLINT.name(), 0, 0);
        Short[] dataArray = {null, 12345, 0, Short.MIN_VALUE, Short.MAX_VALUE};
        for (Short data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            Short deserializedData = (Short) serializer.deserialize(serializedData);
            assertEquals(data, deserializedData);
        }
    }
    
    @Test
    public void testSmallIntWithDriver() throws AAPSDKException {
        String type = "smallint";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.SMALLINT.name(), 0, 0);
        Short[] dataArray = {12345, 0, Short.MIN_VALUE, Short.MAX_VALUE};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (Short data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                Short deserializedData = (Short) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(deserializedData,
                            (Short) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (Short data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data) {
                    // pass
                } else {
                    assertEquals((Short) rsAE.getShort(1),
                            (Short) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testSmallmoney() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.SMALLMONEY.name(), 5, 0);
        BigDecimal[] dataArray = {new BigDecimal(0L), new BigDecimal(10000L), new BigDecimal(-1000L)};
        for (BigDecimal data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            BigDecimal deserializedData = (BigDecimal) serializer.deserialize(serializedData);
            if (null == data && null == deserializedData) {
                // pass
            } else {
                assertEquals(data.stripTrailingZeros(), deserializedData.stripTrailingZeros());
            }
        }
    }
    
    @Test
    public void testSmallmoneyWithDriver() throws AAPSDKException {
        String type = "smallmoney";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.SMALLMONEY.name(), 5, 0);
        BigDecimal[] dataArray = {new BigDecimal(0L), new BigDecimal(10000L), new BigDecimal(-1000L)};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (BigDecimal data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                BigDecimal deserializedData = (BigDecimal) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(deserializedData,
                            (BigDecimal) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (BigDecimal data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data) {
                    // pass
                } else {
                    assertEquals((BigDecimal) rsAE.getMoney(1),
                            (BigDecimal) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }


    @Test
    public void testTime() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.TIME.name(), 0, 0);
        java.sql.Time[] dataArray = {new java.sql.Time(1600991197216L), new java.sql.Time(0L),
                new java.sql.Time(9999999999999L)};
        for (java.sql.Time data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            java.sql.Time deserializedData = (java.sql.Time) serializer.deserialize(serializedData);
            assertEquals(data.toString(), deserializedData.toString());
        }
    }
    
    @Test
    public void testTimeWithDriver() throws AAPSDKException {
        String type = "time";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.TIME.name(), 0, 0);
        java.sql.Time[] dataArray = {new java.sql.Time(1600991197216L), new java.sql.Time(0L)};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (java.sql.Time data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                java.sql.Time deserializedData = (java.sql.Time) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    // test decrypted data
                    assertEquals(deserializedData.toString(),
                            ((java.sql.Time) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1)))).toString());
                }
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (java.sql.Time data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data) {
                    // pass
                } else {
                    assertEquals((java.sql.Time) rsAE.getTime(1),
                            (java.sql.Time) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testTinyint() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.TINYINT.name(), 0, 0);
        Short[] dataArray = {null, 12345, 0, Short.MIN_VALUE, Short.MAX_VALUE, 255};
        for (Short data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            Short deserializedData = (Short) serializer.deserialize(serializedData);
            assertEquals(data, deserializedData);
        }
    }
    
    @Test
    public void testTinyintWithDriver() throws AAPSDKException {
        String type = "tinyint";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.TINYINT.name(), 0, 0);
        Short[] dataArray = {0, 255};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (Short data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                Short deserializedData = (Short) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(deserializedData,
                            (Short) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (Short data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data) {
                    // pass
                } else {
                    assertEquals((Short) rsAE.getShort(1),
                            (Short) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testUniqueidentifier() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.GUID.name(), 40, 0);
        String[] dataArray = {"6F9619FF-8B86-D011-B42D-00C04FC964FF", "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"};
        for (String data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            String deserializedData = (String) serializer.deserialize(serializedData);
            assertEquals(data, deserializedData);
        }
    }
    
    @Test
    public void testUniqueidentifierWithDriver() throws AAPSDKException {
        String type = "uniqueidentifier";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.GUID.name(), 40, 0);
        String[] dataArray = {"6F9619FF-8B86-D011-B42D-00C04FC964FF", "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (String data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                String deserializedData = (String) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(deserializedData,
                            (String) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (String data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data) {
                    // pass
                } else {
                    assertEquals((String) rsAE.getUniqueIdentifier(1),
                            (String) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testVarbinary() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.VARBINARY.name(), 400, 0);
        byte[][] dataArray = {null, new byte[] {0}, new byte[] {-128}, new byte[] {127},
                new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
                new byte[] {127, 127, 127, 127, 127, -128, -128, 0, 0, 0},
                new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new byte[] {127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127,
                        127, 127},
                new byte[] {-128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
                        -128, -128, -128, -128, -128}};
        for (byte[] data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            byte[] deserializedData = (byte[]) serializer.deserialize(serializedData);
            assertEquals(data, deserializedData);
        }
    }
    
    @Test
    public void testVarbinaryWithDriver() throws AAPSDKException {
        String type = "varbinary(400)";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.VARBINARY.name(), 400, 0);
        byte[][] dataArray = {null, new byte[] {0}, new byte[] {-128}, new byte[] {127},
                new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
                new byte[] {127, 127, 127, 127, 127, -128, -128, 0, 0, 0},
                new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                new byte[] {127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127,
                        127, 127},
                new byte[] {-128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
                        -128, -128, -128, -128, -128}};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            for (byte[] data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                byte[] deserializedData = (byte[]) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertTrue(Arrays.equals(deserializedData,
                            (byte[]) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1)))));
                }
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (byte[] data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data && null == rsAE.getBytes(1)) {
                    // pass
                } else {
                    assertTrue(Arrays.equals((byte[]) rsAE.getBytes(1),
                            (byte[]) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1)))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    @Test
    public void testVarchar() throws AAPSDKException {
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.VARCHAR.name(), 17, 0);
        String[] dataArray = {null, new String("Test Varchar data"), new String("             ")};
        for (String data : dataArray) {
            byte[] serializedData = serializer.serialize(data);
            String deserializedData = (String) serializer.deserialize(serializedData);
            assertEquals(data, deserializedData);
        }
    }
    
    @Test
    public void testVarcharWithDriver() throws AAPSDKException {
        String type = "varchar(17)";
        ISerializer serializer = SqlSerializerFactory.getOrCreate(SSType.VARCHAR.name(), 17, 0);
        String[] dataArray = {null, new String("Test Varchar data"), new String("             ")};
        try {
            createTable(type);
            populateData(type, dataArray);
            // Deterministic
            SQLServerResultSet rs = retrieveData(randomTableName1);
            int count = 0;
            for (String data : dataArray) {
                rs.next();
                // test encrypted data
                byte[] serializedData = serializer.serialize(data);
                String deserializedData = (String) serializer.deserialize(serializedData);
                if (null == data && null == deserializedData) {
                    // pass
                } else {
                    assertTrue(Arrays.equals(rs.getBytes(1), deterministicAlgorithm.Encrypt(serializedData)));

                    // test decrypted data
                    assertEquals(deserializedData,
                            (String) serializer.deserialize(deterministicAlgorithm.Decrypt(rs.getBytes(1))));
                }
                count++;
            }

            // Randomized
            rs = retrieveData(randomTableName2);
            SQLServerResultSet rsAE = retrieveDataAE(randomTableName1);
            for (String data : dataArray) {
                rs.next();
                rsAE.next();
                // test decrypted data
                if (null == data || data.isEmpty()) {
                    // pass
                } else {
                    assertEquals((String) rsAE.getString(1),
                            (String) serializer.deserialize(randomizedEncryptionAlgorithm.Decrypt(rs.getBytes(1))));
                }
            }
            dropTable();
        } catch (SQLException e) {
            throw new AAPSDKException(e.getMessage());
        }
    }

    private void createTable(String type) throws AAPSDKException {
        try {
            dropTable();
            String collation = type.contains("char") ? " COLLATE Latin1_General_BIN2" : "";
            String createSql = "create table " + randomTableName1 + " (" + "c1 " + type + collation
                    + " ENCRYPTED WITH (ENCRYPTION_TYPE = DETERMINISTIC, ALGORITHM = 'AEAD_AES_256_CBC_HMAC_SHA_256', COLUMN_ENCRYPTION_KEY = "
                    + cekJks + ") NULL" + ");";
            stmt.execute(createSql);
            createSql = "create table " + randomTableName2 + " (" + "c1 " + type + collation
                    + " ENCRYPTED WITH (ENCRYPTION_TYPE = RANDOMIZED, ALGORITHM = 'AEAD_AES_256_CBC_HMAC_SHA_256', COLUMN_ENCRYPTION_KEY = "
                    + cekJks + ") NULL" + ");";
            stmt.execute(createSql);

        } catch (SQLTimeoutException | SQLServerException e) {
            throw new AAPSDKException("Unable to create table : " + e.getMessage());
        }

    }

    private void dropTable() throws AAPSDKException {
        try {
            String dropSql = "if exists (select * from dbo.sysobjects where id = object_id(N'[dbo]." + randomTableName1
                    + "') and OBJECTPROPERTY(id, N'IsUserTable') = 1) DROP TABLE " + randomTableName1 + "";
            stmt.execute(dropSql);
            dropSql = "if exists (select * from dbo.sysobjects where id = object_id(N'[dbo]." + randomTableName2
                    + "') and OBJECTPROPERTY(id, N'IsUserTable') = 1) DROP TABLE " + randomTableName2 + "";
            stmt.execute(dropSql);
        } catch (SQLTimeoutException | SQLServerException e) {
            throw new AAPSDKException("Unable to drop table : " + e.getMessage());
        }
    }

    private void populateData(String type, Object[] dataArray) throws AAPSDKException {
        try (SQLServerConnection AEconnection = (SQLServerConnection) PrepUtil.getConnection(AETestConnectionString);
                SQLServerPreparedStatement pstmt1 = (SQLServerPreparedStatement) AEconnection
                        .prepareStatement("insert into " + randomTableName1 + " values (?)");
                SQLServerPreparedStatement pstmt2 = (SQLServerPreparedStatement) AEconnection
                        .prepareStatement("insert into " + randomTableName2 + " values (?)");) {
            for (Object data : dataArray) {
                switch (type) {
                    case "bigint":
                        pstmt1.setLong(1, (Long) data);
                        pstmt1.execute();
                        pstmt2.setLong(1, (Long) data);
                        pstmt2.execute();
                        break;
                    case "binary(5)":
                    case "varbinary(400)":
                        pstmt1.setBytes(1, (byte[]) data);
                        pstmt1.execute();
                        pstmt2.setBytes(1, (byte[]) data);
                        pstmt2.execute();
                        break;
                    case "bit":
                        pstmt1.setBoolean(1, (boolean) data);
                        pstmt1.execute();
                        pstmt2.setBoolean(1, (boolean) data);
                        pstmt2.execute();
                        break;
                    case "char(25)":
                    case "varchar(17)":
                        pstmt1.setString(1, (String) data);
                        pstmt1.execute();
                        pstmt2.setString(1, (String) data);
                        pstmt2.execute();
                        break;
                    case "date":
                        pstmt1.setDate(1, (java.sql.Date) data);
                        pstmt1.execute();
                        pstmt2.setDate(1, (java.sql.Date) data);
                        pstmt2.execute();
                        break;
                    case "datetime":
                        pstmt1.setDateTime(1, (java.sql.Timestamp) data);
                        pstmt1.execute();
                        pstmt2.setDateTime(1, (java.sql.Timestamp) data);
                        pstmt2.execute();
                        break;
                    case "datetime2":
                        pstmt1.setTimestamp(1, (java.sql.Timestamp) data);
                        pstmt1.execute();
                        pstmt2.setTimestamp(1, (java.sql.Timestamp) data);
                        pstmt2.execute();
                        break;
                    case "datetimeoffset":
                        pstmt1.setDateTimeOffset(1,
                                microsoft.sql.DateTimeOffset.valueOf(((DateTimeOffset) data).getTimestamp(), 0));
                        pstmt1.execute();
                        pstmt2.setDateTimeOffset(1,
                                microsoft.sql.DateTimeOffset.valueOf(((DateTimeOffset) data).getTimestamp(), 0));
                        pstmt2.execute();
                        break;
                    case "decimal(38, 4)":
                        pstmt1.setBigDecimal(1, (BigDecimal) data, 38, 4);
                        pstmt1.execute();
                        pstmt2.setBigDecimal(1, (BigDecimal) data, 38, 4);
                        pstmt2.execute();
                        break;
                    case "float":
                        pstmt1.setDouble(1, (Double) data);
                        pstmt1.execute();
                        pstmt2.setDouble(1, (Double) data);
                        pstmt2.execute();
                        break;
                    case "integer":
                        if (data == null) {
                            pstmt1.setNull(1, JDBCType.INTEGER.getIntValue());
                            pstmt1.execute();
                            pstmt2.setNull(1, JDBCType.INTEGER.getIntValue());
                            pstmt2.execute();
                        } else {
                            pstmt1.setInt(1, (Integer) data);
                            pstmt1.execute();
                            pstmt2.setInt(1, (Integer) data);
                            pstmt2.execute();
                        }
                        break;
                    case "money":
                        pstmt1.setMoney(1, (BigDecimal) data);
                        pstmt1.execute();
                        pstmt2.setMoney(1, (BigDecimal) data);
                        pstmt2.execute();
                        break;
                    case "nchar(10)":
                    case "nvarchar(40)":
                        pstmt1.setNString(1, (String) data);
                        pstmt1.execute();
                        pstmt2.setNString(1, (String) data);
                        pstmt2.execute();
                        break;
                    case "real":
                        pstmt1.setFloat(1, (Float) data);
                        pstmt1.execute();
                        pstmt2.setFloat(1, (Float) data);
                        pstmt2.execute();
                        break;
                    case "smalldatetime":
                        pstmt1.setSmallDateTime(1, (java.sql.Timestamp) data);
                        pstmt1.execute();
                        pstmt2.setSmallDateTime(1, (java.sql.Timestamp) data);
                        pstmt2.execute();
                        break;
                    case "smallint":
                    case "tinyint":
                        pstmt1.setShort(1, (Short) data);
                        pstmt1.execute();
                        pstmt2.setShort(1, (Short) data);
                        pstmt2.execute();
                        break;
                    case "smallmoney":
                        pstmt1.setSmallMoney(1, (BigDecimal) data);
                        pstmt1.execute();
                        pstmt2.setSmallMoney(1, (BigDecimal) data);
                        pstmt2.execute();
                        break;
                    case "time":
                        pstmt1.setTime(1, (java.sql.Time) data);
                        pstmt1.execute();
                        pstmt2.setTime(1, (java.sql.Time) data);
                        pstmt2.execute();
                        break;
                    case "uniqueidentifier":
                        pstmt1.setUniqueIdentifier(1, (String) data);
                        pstmt1.execute();
                        pstmt2.setUniqueIdentifier(1, (String) data);
                        pstmt2.execute();
                        break;
                    default:
                        throw new AAPSDKException("Invalid type :" + type);
                }
            }
        } catch (SQLException e) {
            throw new AAPSDKException("Unable to populate table : " + e.getMessage());
        }
    }

    private SQLServerResultSet retrieveData(String tableName) throws AAPSDKException {
        SQLServerResultSet rs;
        try {
            String selectSql = "select * from " + tableName;
            rs = (SQLServerResultSet) stmt.executeQuery(selectSql);
        } catch (SQLTimeoutException | SQLServerException e) {
            throw new AAPSDKException("Unable to drop table " + tableName + " : " + e.getMessage());
        }

        return rs;
    }

    private SQLServerResultSet retrieveDataAE(String tableName) throws AAPSDKException {
        SQLServerResultSet rs;
        try {
            String selectSql = "select * from " + tableName;
            rs = (SQLServerResultSet) stmtAE.executeQuery(selectSql);
        } catch (SQLTimeoutException | SQLServerException e) {
            throw new AAPSDKException("Unable to drop table " + tableName + " : " + e.getMessage());
        }

        return rs;
    }
}
