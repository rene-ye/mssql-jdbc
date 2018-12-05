package com.microsoft.sqlserver.jdbc.resiliency;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.microsoft.sqlserver.testframework.AbstractTest;


public class ReflectiveTests extends AbstractTest {

    @Test
    public void testTimeout() throws SQLException {
        Map<String, String> m = new HashMap<>();
        m.put("queryTimeout", "5");
        m.put("loginTimeout", "6");
        m.put("connectRetryCount", "2");
        String cs = ResiliencyUtils.setConnectionProps(connectionString.concat(";"), m);
        try (Connection c = DriverManager.getConnection(cs)) {
            try (Statement s = c.createStatement()) {
                ResiliencyUtils.killConnection(c, connectionString);
                ResiliencyUtils.blockConnection(c);
                s.executeQuery("SELECT 1");
            } catch (SQLException e) {
                assertTrue("Unexpected exception caught: " + e.getMessage(),
                        e.getMessage().contains("The query has timed out."));
            }
        }
    }

    @Test
    public void testDefaultRetryDelay() throws SQLException {
        try (Connection c = DriverManager.getConnection(connectionString)) {
            try (Statement s = c.createStatement()) {
                ResiliencyUtils.killConnection(c, connectionString);
                ResiliencyUtils.blockConnection(c);
                s.executeQuery("SELECT 1");
            } catch (SQLException e) {
                assertTrue("Unexpected exception caught: " + e.getMessage(), e.getMessage().contains("timeout"));
            }
        }
    }

    @Test
    public void testRetryIntervalGreaterThanQueryTimeout() throws SQLException {
        Map<String, String> m = new HashMap<>();
        m.put("queryTimeout", "5");
        m.put("retryInterval", "30");
        String cs = ResiliencyUtils.setConnectionProps(connectionString.concat(";"), m);
        try (Connection c = DriverManager.getConnection(cs)) {
            try (Statement s = c.createStatement()) {
                ResiliencyUtils.killConnection(c, connectionString);
                s.executeQuery("SELECT 1");
                fail("No exception was thrown when queryTimeout value was less than retryInterval.");
            } catch (SQLException e) {
                // queryTimeout < retryInterval error
                assertTrue("Unexpected exception caught: " + e.getMessage(),
                        e.getMessage().contains("The query has timed out."));
            }
        }
    }

    @Test
    /*
     * Command with infinite ConnectionTimeout and ReconnectRetryCount == 1 is executed over a broken connection
     * Expected: Client times out by QueryTimeout
     */
    public void testInfiniteLoginTimeout() throws SQLException {
        long startTime = 0;
        
        Map<String, String> m = new HashMap<>();
        m.put("queryTimeout", "5");
        m.put("connectRetryCount", "1");
        String cs = ResiliencyUtils.setConnectionProps(connectionString.concat(";"), m);
        try (Connection c = DriverManager.getConnection(cs)) {
            try (Statement s = c.createStatement()) {
                ResiliencyUtils.killConnection(c, connectionString);
                ResiliencyUtils.blockConnection(c);
                startTime = System.currentTimeMillis();
                s.executeQuery("SELECT 1");
            } catch (SQLException e) {
                long endTime = System.currentTimeMillis();
                double elapsedTime = (endTime - startTime)/1000;
                assertTrue("Elapsed Time out of Range: " + elapsedTime, elapsedTime < 6);
            }
        }
    }
}
