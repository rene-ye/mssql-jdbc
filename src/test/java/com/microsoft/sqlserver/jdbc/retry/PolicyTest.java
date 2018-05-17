package com.microsoft.sqlserver.jdbc.retry;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import com.microsoft.sqlserver.testframework.AbstractTest;

import net.jodah.failsafe.RetryPolicy;

@RunWith(JUnitPlatform.class)
public class PolicyTest extends AbstractTest {
    
    private boolean abortingErrorCodes(int errCode) {
        System.out.println(errCode);
        List<Integer> doNotReturn = Arrays.asList(
                18456,
                18488,
                18486,
                4,
                5,
                6,
                8,
                9
                );
        return doNotReturn.contains(errCode);
    }

    /**
     * Connect with a permissive Trust Manager that always accepts the X509Certificate chain offered to it.
     * 
     * @throws Exception
     */
    @Test
    public void testWithPermissiveX509TrustManager() throws Exception {
        String url = connectionString;


        RetryPolicy rp = new RetryPolicy()
                .withBackoff(100,5000,TimeUnit.MILLISECONDS)
                .withJitter(50, TimeUnit.MILLISECONDS)
                .withMaxDuration(10, TimeUnit.SECONDS)
                .abortOn(failure -> abortingErrorCodes(((SQLException) failure).getErrorCode()));
    }
}
