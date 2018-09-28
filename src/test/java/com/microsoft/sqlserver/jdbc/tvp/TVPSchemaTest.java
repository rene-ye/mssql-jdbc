/*
 * Microsoft JDBC Driver for SQL Server Copyright(c) Microsoft Corporation All rights reserved. This program is made
 * available under the terms of the MIT License. See the LICENSE file in the project root for more information.
 */
package com.microsoft.sqlserver.jdbc.tvp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.microsoft.sqlserver.jdbc.SQLServerCallableStatement;
import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement;
import com.microsoft.sqlserver.testframework.AbstractTest;
import com.microsoft.sqlserver.testframework.DBConnection;
import com.microsoft.sqlserver.testframework.DBResultSet;
import com.microsoft.sqlserver.testframework.DBStatement;


@RunWith(JUnitPlatform.class)
public class TVPSchemaTest extends AbstractTest {

    private static DBConnection conn = null;
    static DBStatement stmt = null;
    static DBResultSet rs = null;
    static SQLServerDataTable tvp = null;
    static String expectecValue1 = "hello";
    static String expectecValue2 = "world";
    static String expectecValue3 = "again";
    private static String schemaName = "anotherSchma";
    private static String tvpNameWithouSchema = "charTVP";
    private static String tvpNameWithSchema = "[" + schemaName + "].[" + tvpNameWithouSchema + "]";
    private static String charTable = "[" + schemaName + "].[tvpCharTable]";
    private static String procedureName = "[" + schemaName + "].[procedureThatCallsTVP]";

    /**
     * PreparedStatement with storedProcedure
     * 
     * @throws SQLException
     */
    @Test
    @DisplayName("TVPSchemaPreparedStatementStoredProcedure()")
    public void testTVPSchemaPreparedStatementStoredProcedure() throws SQLException {

        final String sql = "{call " + procedureName + "(?)}";

        try (SQLServerPreparedStatement P_C_statement = (SQLServerPreparedStatement) connection.prepareStatement(sql)) {
            P_C_statement.setStructured(1, tvpNameWithSchema, tvp);
            P_C_statement.execute();

            rs = stmt.executeQuery("select * from " + charTable);
            verify(rs);
        }
    }

    /**
     * callableStatement with StoredProcedure
     * 
     * @throws SQLException
     */
    @Test
    @DisplayName("TVPSchemaCallableStatementStoredProcedure()")
    public void testTVPSchemaCallableStatementStoredProcedure() throws SQLException {

        final String sql = "{call " + procedureName + "(?)}";

        try (SQLServerCallableStatement P_C_statement = (SQLServerCallableStatement) connection.prepareCall(sql)) {
            P_C_statement.setStructured(1, tvpNameWithSchema, tvp);
            P_C_statement.execute();

            rs = stmt.executeQuery("select * from " + charTable);
            verify(rs);
        }
    }

    /**
     * Prepared with InsertCommand
     * 
     * @throws SQLException
     * @throws IOException
     */
    @Test
    @DisplayName("TVPSchemaPreparedInsertCommand")
    public void testTVPSchemaPreparedInsertCommand() throws SQLException, IOException {

        try (SQLServerPreparedStatement P_C_stmt = (SQLServerPreparedStatement) connection
                .prepareStatement("INSERT INTO " + charTable + " select * from ? ;")) {
            P_C_stmt.setStructured(1, tvpNameWithSchema, tvp);
            P_C_stmt.executeUpdate();

            rs = stmt.executeQuery("select * from " + charTable);
            verify(rs);
        }
    }

    /**
     * Callable with InsertCommand
     * 
     * @throws SQLException
     * @throws IOException
     */
    @Test
    @DisplayName("TVPSchemaCallableInsertCommand()")
    public void testTVPSchemaCallableInsertCommand() throws SQLException, IOException {

        try (SQLServerCallableStatement P_C_stmt = (SQLServerCallableStatement) connection
                .prepareCall("INSERT INTO " + charTable + " select * from ? ;")) {
            P_C_stmt.setStructured(1, tvpNameWithSchema, tvp);
            P_C_stmt.executeUpdate();

            rs = stmt.executeQuery("select * from " + charTable);
            verify(rs);
        }
    }

    @BeforeEach
    public void testSetup() throws SQLException {
        conn = new DBConnection(connectionString);
        stmt = conn.createStatement();

        dropProcedure();
        dropTables();
        dropTVPS();

        dropAndCreateSchema();

        createTVPS();
        createTables();
        createPreocedure();

        tvp = new SQLServerDataTable();
        tvp.addColumnMetadata("PlainChar", java.sql.Types.CHAR);
        tvp.addColumnMetadata("PlainVarchar", java.sql.Types.VARCHAR);
        tvp.addColumnMetadata("PlainVarcharMax", java.sql.Types.VARCHAR);

        tvp.addRow(expectecValue1, expectecValue2, expectecValue3);
        tvp.addRow(expectecValue1, expectecValue2, expectecValue3);
        tvp.addRow(expectecValue1, expectecValue2, expectecValue3);
        tvp.addRow(expectecValue1, expectecValue2, expectecValue3);
        tvp.addRow(expectecValue1, expectecValue2, expectecValue3);
    }

    private void verify(DBResultSet rs) throws SQLException {
        while (rs.next()) {
            String actualValue1 = rs.getString(1);
            String actualValue2 = rs.getString(2);
            String actualValue3 = rs.getString(3);

            assertEquals(actualValue1.trim(), expectecValue1);
            assertEquals(actualValue2.trim(), expectecValue2);
            assertEquals(actualValue3.trim(), expectecValue3);
        }
    }

    private void dropProcedure() throws SQLException {
        String sql = " IF EXISTS (select * from sysobjects where id = object_id(N'" + procedureName
                + "') and OBJECTPROPERTY(id, N'IsProcedure') = 1)" + " DROP PROCEDURE " + procedureName;
        stmt.execute(sql);
    }

    private static void dropTables() throws SQLException {
        stmt.executeUpdate("if object_id('" + charTable + "','U') is not null" + " drop table " + charTable);
    }

    private static void dropTVPS() throws SQLException {
        stmt.executeUpdate("IF EXISTS (SELECT * FROM sys.types WHERE is_table_type = 1 AND name = '"
                + tvpNameWithouSchema + "') " + " drop type " + tvpNameWithSchema);
    }

    private static void dropAndCreateSchema() throws SQLException {
        stmt.execute("if EXISTS (SELECT * FROM sys.schemas where name = 'anotherSchma') drop schema anotherSchma");
        stmt.execute("CREATE SCHEMA anotherSchma");
    }

    private static void createPreocedure() throws SQLException {
        String sql = "CREATE PROCEDURE " + procedureName + " @InputData " + tvpNameWithSchema + " READONLY " + " AS "
                + " BEGIN " + " INSERT INTO " + charTable + " SELECT * FROM @InputData" + " END";

        stmt.execute(sql);
    }

    private void createTables() throws SQLException {
        String sql = "create table " + charTable + " (" + "PlainChar char(50) null," + "PlainVarchar varchar(50) null,"
                + "PlainVarcharMax varchar(max) null," + ");";
        stmt.execute(sql);
    }

    private void createTVPS() throws SQLException {
        String TVPCreateCmd = "CREATE TYPE " + tvpNameWithSchema + " as table ( " + "PlainChar char(50) null,"
                + "PlainVarchar varchar(50) null," + "PlainVarcharMax varchar(max) null" + ")";
        stmt.executeUpdate(TVPCreateCmd);
    }

    @AfterEach
    public void terminateVariation() throws SQLException {
        if (null != conn) {
            conn.close();
        }
        if (null != stmt) {
            stmt.close();
        }
        if (null != rs) {
            rs.close();
        }
        if (null != tvp) {
            tvp.clear();
        }
    }

}
