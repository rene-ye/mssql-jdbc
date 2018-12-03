package com.microsoft.sqlserver.jdbc.resiliency;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import com.microsoft.sqlserver.testframework.AbstractTest;


public class SessionStateTests extends AbstractTest {

    @Test
    public void testParseInitial() throws SQLException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException {
        Map<String, String> m = new HashMap<>();
        m.put("connectRetryCount", "1");
        String cs = ResiliencyUtils.setConnectionProps(connectionString.concat(";"), m);

        try (Connection c = DriverManager.getConnection(cs)) {
            Field fields[] = c.getClass().getSuperclass().getDeclaredFields();
            for (Field f : fields) {
                if (f.getName() == "sessionRecovery") {
                    f.setAccessible(true);
                    Object sessionRecoveryFeature = f.get(c);
                    Method method = sessionRecoveryFeature.getClass().getDeclaredMethod("isConnectionRecoveryNegotiated");
                    method.setAccessible(true);
                    boolean b = (boolean) method.invoke(sessionRecoveryFeature);
                    assertTrue("Session Recovery not negotiated when requested", b);
                }
            }
        }
    }
}
