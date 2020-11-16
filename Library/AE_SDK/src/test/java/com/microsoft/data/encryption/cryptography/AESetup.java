/*
 * Microsoft JDBC Driver for SQL Server Copyright(c) Microsoft Corporation All rights reserved. This program is made
 * available under the terms of the MIT License. See the LICENSE file in the project root for more AEInformation.
 */
package com.microsoft.data.encryption.cryptography;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import com.microsoft.sqlserver.jdbc.SQLServerStatement;


/**
 * Setup for Always Encrypted test This test will work on Azure DevOps as java key store gets created from the .yml
 * scripts. Users on their local machine should create the keystore manually and save the alias name in JavaKeyStore.txt
 * file. For local test purposes, put this in the target/test-classes directory
 *
 */
@RunWith(JUnitPlatform.class)
public class AESetup extends AbstractTest {

    protected static String AETestConnectionString = null;
    protected static SQLServerConnection connectionAE = null;
    protected static SQLServerStatement stmtAE = null;


    @BeforeAll
    public static void setupAE() throws Exception {
        AETestConnectionString = connectionString + ";keyStoreAuthentication=JavaKeyStorePassword;keyStoreLocation="
                + javaKeyPath + ";keyStoreSecret=" + Constants.JKS_SECRET +";sendTimeAsDateTime=false" + ";columnEncryptionSetting=enabled;";
        readFromFile(Constants.JAVA_KEY_STORE_FILENAME, "Alias name");

        createCMK(cmkJks, Constants.JAVA_KEY_STORE_NAME, javaKeyAliases, Constants.CMK_SIGNATURE);
        createCEK(cmkJks, cekJks, javaKeyProvider);
        
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            if (!SQLServerDriver.isRegistered()) {
                SQLServerDriver.register();
            }
            if (null == connectionAE || connectionAE.isClosed()) {
                connectionAE = (SQLServerConnection) DriverManager.getConnection(AETestConnectionString);
                stmtAE = (SQLServerStatement) connectionAE.createStatement();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Read the alias from file which is created during creating jks If the jks and alias name in JavaKeyStore.txt does
     * not exists, will not run!
     * 
     * @param inputFile
     * @param lookupValue
     * @throws IOException
     */
    private static void readFromFile(String inputFile, String lookupValue) throws IOException {
        String filePath = TestUtils.getCurrentClassPath();
        try {
            File f = new File(filePath + inputFile);
            try (BufferedReader buffer = new BufferedReader(new FileReader(f))) {
                String readLine = "";
                String[] linecontents;

                while ((readLine = buffer.readLine()) != null) {
                    if (readLine.trim().contains(lookupValue)) {
                        linecontents = readLine.split(" ");
                        javaKeyAliases = linecontents[2];
                        break;
                    }
                }
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Create column master key
     * 
     * @param keyStoreName
     * @param keyPath
     * @throws SQLException
     */
    private static void createCMK(String cmkName, String keyStoreName, String keyPath,
            String signature) throws SQLException {
        try (SQLServerConnection con = (SQLServerConnection) PrepUtil.getConnection(AETestConnectionString);
                SQLServerStatement stmt = (SQLServerStatement) con.createStatement()) {
            String sql = " if not exists (SELECT name from sys.column_master_keys where name='" + cmkName + "')"
                    + " begin" + " CREATE COLUMN MASTER KEY " + cmkName + " WITH (KEY_STORE_PROVIDER_NAME = '"
                    + keyStoreName + "', KEY_PATH = '" + keyPath + "') end";
            stmt.execute(sql);
        }
    }

    /**
     * Create column encryption key
     * 
     * @param storeProvider
     * @param certStore
     * @throws SQLException
     * @throws AAPSDKException
     */
    private static void createCEK(String cmkName, String cekName,
            EncryptionJavaKeyStoreProvider storeProvider) throws SQLException, AAPSDKException {
        try (SQLServerConnection con = (SQLServerConnection) PrepUtil.getConnection(AETestConnectionString);
                SQLServerStatement stmt = (SQLServerStatement) con.createStatement()) {
            byte[] valuesDefault = Constants.CEK_STRING.getBytes();
            String encryptedValue;

            if (storeProvider instanceof EncryptionJavaKeyStoreProvider) {
                byte[] key = storeProvider.encryptEncryptionKey(javaKeyAliases, Constants.CEK_ALGORITHM, valuesDefault);
                encryptedValue = "0x" + TestUtils.bytesToHexString(key, key.length);
            } else {
                encryptedValue = Constants.CEK_ENCRYPTED_VALUE;
            }

            String sql = "if not exists (SELECT name from sys.column_encryption_keys where name='" + cekName + "')"
                    + " begin" + " CREATE COLUMN ENCRYPTION KEY " + cekName + " WITH VALUES " + "(COLUMN_MASTER_KEY = "
                    + cmkName + ", ALGORITHM = '" + Constants.CEK_ALGORITHM + "', ENCRYPTED_VALUE = " + encryptedValue
                    + ") end;";
            stmt.execute(sql);
        }
    }

    /**
     * Dropping all CMKs and CEKs and any open resources. Technically, dropAll depends on the state of the class so it
     * shouldn't be static, but the AfterAll annotation requires it to be static.
     * 
     * @throws SQLException
     */
    @AfterAll
    public static void cleanUp() throws Exception {
        dropAll();
        if (null != connection) {
            connection.close();
        }
    }

    /**
     * Dropping all CMKs and CEKs and any open resources. Technically, dropAll depends on the state of the class so it
     * shouldn't be static, but the AfterAll annotation requires it to be static.
     * 
     * @throws SQLException
     */
    public static void dropAll() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            dropCEK(cekJks, stmt);
            dropCMK(cmkJks, stmt);
        }
    }

    /**
     * Dropping column encryption key
     * 
     * @throws SQLException
     */
    private static void dropCEK(String cekName, Statement stmt) throws SQLException {
        String cekSql = " if exists (SELECT name from sys.column_encryption_keys where name='" + cekName + "')"
                + " begin" + " drop column encryption key " + cekName + " end";
        stmt.execute(cekSql);
    }

    /**
     * Dropping column master key
     * 
     * @throws SQLException
     */
    private static void dropCMK(String cmkName, Statement stmt) throws SQLException {
        String cekSql = " if exists (SELECT name from sys.column_master_keys where name='" + cmkName + "')" + " begin"
                + " drop column master key " + cmkName + " end";
        stmt.execute(cekSql);
    }
}
