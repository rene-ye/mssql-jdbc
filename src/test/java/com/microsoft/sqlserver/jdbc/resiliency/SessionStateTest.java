package com.microsoft.sqlserver.jdbc.resiliency;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.sqlserver.jdbc.TestUtils;
import com.microsoft.sqlserver.testframework.AbstractTest;

public class SessionStateTest extends AbstractTest {
    
    @Test
    public void testSessionOptions() throws SQLException {
        try (Connection c = DriverManager.getConnection(connectionString)) {
            ResiliencyUtils.toggleRandomProperties(c);
            Map<String,String> dbProperties = ResiliencyUtils.getUserOptions(c);
            //ResiliencyUtils.killConnection(c,connectionString);
            Map<String,String> reconnectProperties = ResiliencyUtils.getUserOptions(c);
            assertEquals(dbProperties, reconnectProperties);
        }
    }
    
    @Test
    public void testBasicRead() throws SQLException {
        try (Connection c = DriverManager.getConnection(connectionString)) {
            try (Statement stmt = c.createStatement()) {
                String table = "table"+UUID.randomUUID();
                TestUtils.dropTableIfExists(table, stmt);
                stmt.execute("CREATE TABLE " + table + " (id int)");
                stmt.execute("INSERT INTO " + table + " VALUES 1");
                ResiliencyUtils.killConnection(c, connectionString);
                stmt.execute("INSERT INTO " + table + " VALUES 2");
                ResiliencyUtils.killConnection(c, connectionString);
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM " + table)) {
                    while (rs.next()) {
                        int expected = 0;
                        assertEquals(++expected, rs.getInt(1));
                    }
                }                
            }
        }
    }   
}
