package com.microsoft.sqlserver.jdbc.retry;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import com.microsoft.sqlserver.testframework.AbstractTest;

import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.function.CheckedRunnable;

@RunWith(JUnitPlatform.class)
public class PolicyTest extends AbstractTest {
    
    private boolean abortingErrorCodes(int errCode) {
        System.out.println("Error Code: " + errCode);
        List<Integer> doNotReturn = Arrays.asList(
                18456,//LOGON FAILED
                18488,//PASSWORD EXPIRED
                18486,//USER ACCOUNT LOCKED
                4,//INVALID TDS
                5,//SSL FAILED
                6,//UNSUPPORTED CONFIG
                7,//INTERMITTENT TLS FAILED
                8,//SOCKET TIMEOUT
                9 //QUERY TIMEOUT
                );
        Optional<Integer> result = doNotReturn.stream().parallel()
                .filter(num -> num == errCode)
                .findAny();
        return result.isPresent();
    }
    
    private class throwErrCode implements CheckedRunnable {
        int errCode, attemptCount = 0;
        
        public throwErrCode(int i) {
            this.errCode = i;
        }
        
        public void run() throws SQLException {
            System.out.println("Attempt Number: " + ++attemptCount);
            SQLServerException e = new SQLServerException("Simulated Exception", null, errCode, null);
            throw e;
        }
    }

    @Test
    public void testRetryPolicy() throws Exception {
        RetryPolicy rp = new RetryPolicy()
                .withBackoff(100,5000,TimeUnit.MILLISECONDS)
                .withJitter(50, TimeUnit.MILLISECONDS)
                .withMaxDuration(10, TimeUnit.SECONDS)
                .abortOn(failure -> abortingErrorCodes(((SQLException) failure).getErrorCode()));
        
        Failsafe.with(rp)
        .run(new throwErrCode(18456));
    }
}
