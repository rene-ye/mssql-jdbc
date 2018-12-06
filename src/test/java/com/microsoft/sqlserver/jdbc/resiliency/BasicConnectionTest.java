package com.microsoft.sqlserver.jdbc.resiliency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.microsoft.sqlserver.jdbc.RandomUtil;
import com.microsoft.sqlserver.jdbc.TestUtils;
import com.microsoft.sqlserver.testframework.AbstractTest;


public class BasicConnectionTest extends AbstractTest {

    @Test
    /*
     * Command is executed over a broken connection (direct or routed)
     * Expected: Connection is re-established and session is restored on the server before command execution
     */
    public void testBasicReconnectDefault() throws SQLException {
        basicReconnect(connectionString);
    }

    @Test
    public void testBasicEncryptedConnection() throws SQLException {
        basicReconnect(connectionString + ";encrypt=true;trustServerCertificate=true;");
    }
    
    
    @Test
    public void testSetAttributes() throws SQLException {
        try (Connection c = DriverManager.getConnection(connectionString)) {
            ResiliencyUtils.toggleRandomProperties(c);
            Map<String,String> expected = ResiliencyUtils.getUserOptions(c);
            ResiliencyUtils.killConnection(c, connectionString);
            Map<String,String> recieved = ResiliencyUtils.getUserOptions(c);
            assertTrue("User options do not match", expected == recieved);
        }
    }

    /*
     * Command with ReconnectRetryCount == 0 is executed over a broken connection
     * Expected: Client reports communication link failure immediately
     */
    @Test
    public void testNoReconnect() throws SQLException {
        try (Connection c = DriverManager.getConnection(connectionString + ";connectRetryCount=0")) {
            try (Statement s = c.createStatement()) {
                ResiliencyUtils.killConnection(c, connectionString);
                c.close();
                s.executeQuery("SELECT 1");
                fail("Query execution did not throw an exception on a closed execution");
            } catch (SQLException e) {
                assertEquals("08S01", e.getSQLState());
            }
        }
    }

    // @Test
    public void testCatalog() throws SQLException {
        String databaseName = null;
        try (Connection c = DriverManager.getConnection(connectionString); Statement s = c.createStatement()) {
            try {
                databaseName = RandomUtil.getIdentifier("resDB");
                TestUtils.dropDatabaseIfExists(databaseName, s);
                s.execute("CREATE DATABASE [" + databaseName + "]");
                try {
                    c.setCatalog(databaseName);
                } catch (SQLException e) {
                    // Switching databases is not supported against Azure, skip/
                    return;
                }
                ResiliencyUtils.killConnection(c, connectionString);
                try (ResultSet rs = s.executeQuery("SELECT db_name();")) {
                    while (rs.next()) {
                        // Check if the driver reconnected to the expected database.
                        assertEquals(databaseName, rs.getString(1));
                    }
                }
            } finally {
                TestUtils.dropDatabaseIfExists(databaseName, s);
            }
        }
    }

    private void basicReconnect(String connectionString) throws SQLException {
        try (Connection c = DriverManager.getConnection(connectionString)) {
            try (Statement s = c.createStatement()) {
                ResiliencyUtils.killConnection(c, connectionString);
                s.executeQuery("SELECT 1");
            }
        }
    }
}
