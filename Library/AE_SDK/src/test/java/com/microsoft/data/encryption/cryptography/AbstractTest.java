package com.microsoft.data.encryption.cryptography;

import java.sql.DriverManager;

import org.junit.jupiter.api.BeforeAll;

import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import com.microsoft.sqlserver.jdbc.SQLServerStatement;


public abstract class AbstractTest {

    protected static SQLServerConnection connection = null;
    protected static SQLServerStatement stmt = null;
    protected static String connectionString = null;
    protected static String javaKeyPath = null;
    protected static String javaKeyAliases = null;
    protected static String[] keyIDs = null;
    
    protected static String cmkJks = Constants.CMK_NAME + "_JKS";
    protected static String cekJks = Constants.CEK_NAME + "_JKS";
    
    public static EncryptionJavaKeyStoreProvider javaKeyProvider = null;
    
    public static final String MSSQL_JDBC_TEST_CONNECTION_PROPERTIES = "mssql_jdbc_test_connection_properties";
    
    public static byte[] rootKey = {97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97,
            97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97};
    public static ProtectedDataEncryptionKey dataEncryptionKey = null;
    public static AeadAes256CbcHmac256EncryptionAlgorithm deterministicAlgorithm = null;
    public static AeadAes256CbcHmac256EncryptionAlgorithm randomizedEncryptionAlgorithm = null;

    @BeforeAll
    public static void setup() throws Exception {
        connectionString = getConfiguredPropertyOrEnv(MSSQL_JDBC_TEST_CONNECTION_PROPERTIES);
        javaKeyPath = TestUtils.getCurrentClassPath() + Constants.JKS_NAME;
        
        if (null == javaKeyProvider) {
            javaKeyProvider = new EncryptionJavaKeyStoreProvider(javaKeyPath,
                    Constants.JKS_SECRET.toCharArray());
        }
        
        dataEncryptionKey = new ProtectedDataEncryptionKey("CEK", rootKey);
        deterministicAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm.getOrCreate(dataEncryptionKey,
                EncryptionType.Deterministic);
        randomizedEncryptionAlgorithm = AeadAes256CbcHmac256EncryptionAlgorithm.getOrCreate(dataEncryptionKey,
                EncryptionType.Randomized);
        keyIDs = getConfiguredProperty("keyID", "").split(Constants.SEMI_COLON);

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            if (!SQLServerDriver.isRegistered()) {
                SQLServerDriver.register();
            }
            if (null == connection || connection.isClosed()) {
                connection = (SQLServerConnection) DriverManager.getConnection(connectionString);
                stmt = (SQLServerStatement) connection.createStatement();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private static String getConfiguredPropertyOrEnv(String key) {
        String value = getConfiguredProperty(key);

        if (null == value) {
            return System.getenv(key);
        }

        return value;
    }

    protected static String getConfiguredProperty(String key) {
        String value = System.getProperty(key);

        return value;
    }

    /**
     * Read property from system or config properties file if not set return default value
     * 
     * @param key
     * @return property value or default value
     */
    protected static String getConfiguredProperty(String key, String defaultValue) {
        String value = getConfiguredProperty(key);

        if (null == value) {
            return defaultValue;
        }

        return value;
    }
}
