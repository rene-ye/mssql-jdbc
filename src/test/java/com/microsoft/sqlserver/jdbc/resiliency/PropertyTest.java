package com.microsoft.sqlserver.jdbc.resiliency;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;

import com.microsoft.sqlserver.testframework.AbstractTest;

public class PropertyTest extends AbstractTest {
    
    private void testInvalidPropertyOverBrokenConnection(String prop, String val, String expectedErrMsg) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append(connectionString).append(";").append(prop).append("=").append(val).append(";");
        try (Connection c = DriverManager.getConnection(sb.toString())) {
            try (Statement s = c.createStatement()) {
                ResiliencyUtils.killConnection(c, connectionString);
                s.executeQuery("SELECT 1");
                fail("No exception caught when " + prop + "=" + val);
            }
        } catch (SQLException e) {
            assertTrue("Unexpected Error Caught: " + e.getMessage(),e.getMessage().contains(expectedErrMsg));
        }
    }
    
    @Test
    public void testRetryCount() throws SQLException {
        // fail immediately without retrying
        testInvalidPropertyOverBrokenConnection("connectRetryCount", "0", "");
        // Out of range, < 0
        testInvalidPropertyOverBrokenConnection("connectRetryCount",
                String.valueOf(ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, 0)), "is not valid.");            
        // Out of range, > 255
        testInvalidPropertyOverBrokenConnection("connectRetryCount",
                String.valueOf(ThreadLocalRandom.current().nextInt(256, Integer.MAX_VALUE)), "is not valid.");
        // non-Integer types: boolean, float, double, string
        testInvalidPropertyOverBrokenConnection("connectRetryCount",
                String.valueOf(ThreadLocalRandom.current().nextBoolean()), "is not valid.");
        testInvalidPropertyOverBrokenConnection("connectRetryCount",
                String.valueOf(ThreadLocalRandom.current().nextFloat()), "is not valid.");
        testInvalidPropertyOverBrokenConnection("connectRetryCount",
                String.valueOf(ThreadLocalRandom.current().nextDouble()), "is not valid.");
        testInvalidPropertyOverBrokenConnection("connectRetryCount",
                ResiliencyUtils.getRandomString(ResiliencyUtils.alpha, 15), "is not valid.");
        //null
        //testInvalidPropertyOverBrokenConnection("connectRetryCount","", "is not valid.");
    }

    
    @Test
    public void testRetryInterval() throws SQLException {
        // Out of range, < 1
        testInvalidPropertyOverBrokenConnection("connectRetryInterval",
                String.valueOf(ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, 1)), "is not valid.");
        // Out of range, > 60
        testInvalidPropertyOverBrokenConnection("connectRetryInterval",
                String.valueOf(ThreadLocalRandom.current().nextInt(61, Integer.MAX_VALUE)), "is not valid.");
        // non-Integer types: boolean, float, double, string
        testInvalidPropertyOverBrokenConnection("connectRetryInterval",
                String.valueOf(ThreadLocalRandom.current().nextBoolean()), "is not valid.");
        testInvalidPropertyOverBrokenConnection("connectRetryInterval",
                String.valueOf(ThreadLocalRandom.current().nextFloat()), "is not valid.");
        testInvalidPropertyOverBrokenConnection("connectRetryInterval",
                String.valueOf(ThreadLocalRandom.current().nextDouble()), "is not valid.");
        testInvalidPropertyOverBrokenConnection("connectRetryInterval",
                ResiliencyUtils.getRandomString(ResiliencyUtils.alpha, 15), "is not valid.");
        //null
        //testInvalidPropertyOverBrokenConnection("connectRetryInterval","", "is not valid.");
    }
}
