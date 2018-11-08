/*
 * Microsoft JDBC Driver for SQL Server Copyright(c) Microsoft Corporation All rights reserved. This program is made
 * available under the terms of the MIT License. See the LICENSE file in the project root for more information.
 */

package com.microsoft.sqlserver.jdbc;

import java.util.concurrent.atomic.AtomicInteger;


class SessionRecoveryFeature {
    private boolean connectionRecoveryNegotiated;
    private int connectRetryCount;
    private SQLServerConnection connection;
    private SessionStateTable sessionStateTable;

    SessionRecoveryFeature(SQLServerConnection connection) {
        this.connection = connection;
    }

    boolean isConnectionRecoveryNegotiated() {
        return connectionRecoveryNegotiated;
    }

    void setConnectionRecoveryNegotiated(boolean connectionRecoveryNegotiated) {
        this.connectionRecoveryNegotiated = connectionRecoveryNegotiated;
    }

    int getConnectRetryCount() {
        return connectRetryCount;
    }

    void setConnectRetryCount(int connectRetryCount) {
        this.connectRetryCount = connectRetryCount;
    }

    SQLServerConnection getConnection() {
        return connection;
    }

    void setConnection(SQLServerConnection connection) {
        this.connection = connection;
    }

    SessionStateTable getSessionStateTable() {
        return sessionStateTable;
    }

    void setSessionStateTable(SessionStateTable sessionStateTable) {
        this.sessionStateTable = sessionStateTable;
    }

    void parseInitialSessionStateData(TDSReader tdsReader, byte[][] sessionStateInitial) throws SQLServerException {
        int bytesRead = 0;
        int dataLength = tdsReader.readInt();

        // Contains StateId, StateLen, StateValue
        while (bytesRead < dataLength) {
            short sessionStateId = (short) tdsReader.readUnsignedByte();
            int sessionStateLength = (int) tdsReader.readUnsignedByte();
            bytesRead += 2;
            if (sessionStateLength >= 0xFF) {
                sessionStateLength = (int) tdsReader.readUnsignedInt(); // xFF - xFFFF
                bytesRead += 2;
            }
            sessionStateInitial[sessionStateId] = new byte[sessionStateLength];
            tdsReader.readBytes(sessionStateInitial[sessionStateId], 0, sessionStateLength);
            bytesRead += sessionStateLength;
        }
    }
}


class SessionStateValue {
    private boolean isRecoverable;
    private int sequenceNumber;
    private int dataLengh;
    private byte[] data;

    boolean isSequenceNumberGreater(int sequenceNumberToBeCompared) {
        // Illustration using 8 bit number

        // Initial assignment takes care of following scenarios:
        // toBeCompared= 2 benchmark = 1 (both positive) ..true
        // toBeCompared=-1(255) benchmark = -2(254) (both negative) ..true
        // toBeCompared=-1(255) benchmark = 1 ..true
        // toBeCompared=-1(255) benchmark = 0 ..true
        boolean greater = true;

        if (sequenceNumberToBeCompared > sequenceNumber) {
            // Following if condition takes care of these scenarios:
            // toBeCompared = 0 benchmark = -1(255)
            // toBeCompared = 1 benchmark = -1(255)
            if ((sequenceNumberToBeCompared >= 0) && (sequenceNumber < 0))
                greater = false;
        }
        // This else takes care of these scenarios where result is false:
        // toBeCompared= 1 benchmark = 2 (both positive) ..false
        // toBeCompared=-2(254) benchmark = -1(255) (both negative) ..false
        else
        // Following if condition to not set return to false for these scenarios:
        // toBeCompared=-1(255) benchmark = 1 ..true
        // toBeCompared=-1(255) benchmark = 0 ..true
        if ((sequenceNumberToBeCompared > 0) || (sequenceNumber < 0))
            greater = false;

        return greater;
    }

    boolean isRecoverable() {
        return isRecoverable;
    }

    void setRecoverable(boolean isRecoverable) {
        this.isRecoverable = isRecoverable;
    }

    int getSequenceNumber() {
        return sequenceNumber;
    }

    void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    int getDataLengh() {
        return dataLengh;
    }

    void setDataLengh(int dataLengh) {
        this.dataLengh = dataLengh;
    }

    byte[] getData() {
        return data;
    }

    void setData(byte[] data) {
        this.data = data;
    }
}


class SessionStateTable {
    private static final int SESSION_STATE_ID_MAX = 256;
    static final long MASTER_RECOVERY_DISABLE_SEQ_NUMBER = 0XFFFFFFFF;
    private boolean masterRecoveryDisabled;
    private byte[][] sessionStateInitial;
    private SessionStateValue sessionStateDelta[];
    private AtomicInteger unRecoverableSessionStateCount = new AtomicInteger(0);

    SessionStateTable() {
        this.sessionStateDelta = new SessionStateValue[SESSION_STATE_ID_MAX];
        this.sessionStateInitial = new byte[SessionStateTable.SESSION_STATE_ID_MAX][];
    }

    void updateSessionState(TDSReader tdsReader, short sessionStateId, int sessionStateLength, int sequenceNumber,
            boolean fRecoverable) throws SQLServerException {
        sessionStateDelta[sessionStateId].setSequenceNumber(sequenceNumber);
        sessionStateDelta[sessionStateId].setDataLengh(sessionStateLength);

        if ((sessionStateDelta[sessionStateId].getData() == null)
                || (sessionStateDelta[sessionStateId].getData().length < sessionStateLength)) {
            sessionStateDelta[sessionStateId].setData(new byte[sessionStateLength]);

            // First time state update and value is not recoverable, hence count is incremented.
            if (!fRecoverable) {
                unRecoverableSessionStateCount.incrementAndGet();
            }
        } else {
            int count;
            // @TODO Where is count supposed to be used?
            // Not a first time state update hence if only there is a transition in state do we update the count.
            if (fRecoverable != sessionStateDelta[sessionStateId].isRecoverable()) {
                count = fRecoverable ? unRecoverableSessionStateCount.decrementAndGet()
                                     : unRecoverableSessionStateCount.incrementAndGet();
            }
        }
        tdsReader.readBytes(sessionStateDelta[sessionStateId].getData(), 0, sessionStateLength);
        sessionStateDelta[sessionStateId].setRecoverable(fRecoverable);
    }

    boolean isMasterRecoveryDisabled() {
        return masterRecoveryDisabled;
    }

    void setMasterRecoveryDisabled(boolean masterRecoveryDisabled) {
        this.masterRecoveryDisabled = masterRecoveryDisabled;
    }

    byte[][] getSessionStateInitial() {
        return sessionStateInitial;
    }

    void setSessionStateInitial(byte[][] sessionStateInitial) {
        this.sessionStateInitial = sessionStateInitial;
    }

    SessionStateValue[] getSessionStateDelta() {
        return sessionStateDelta;
    }

    void setSessionStateDelta(SessionStateValue[] sessionStateDelta) {
        this.sessionStateDelta = sessionStateDelta;
    }
}


final class ReconnectThread implements Runnable {
    private SQLServerConnection con = null;
    private Object reconnectSync = new Object();
    private Object stopReconSync = new Object();
    private SQLServerException eReceived = null;

    private boolean reconnecting = false;
    private volatile boolean stopRequest = false;
    private int connectRetryCount = 0;

    private ReconnectThread() {};

    ReconnectThread(SQLServerConnection sqlC) {
        this.con = sqlC;
    }

    public void run() {
        reconnecting = true;
        try {
            eReceived = null;
            con.connect(null, con.getPooledConnectionParent());
        } catch (SQLServerException se) {
            if (!stopRequest) {
                eReceived = se;
                if (isFatalError(se)) {
                    reconnecting = false;
                } else {
                    try {
                        synchronized (reconnectSync) {
                            reconnectSync.notifyAll();
                        }

                        if (connectRetryCount > 1) {
                            Thread.sleep(con.getRetryInterval() * 1000);
                        }
                    } catch (InterruptedException ie) {
                        // eat it? log it? throw it?
                    }
                }
            }
        } finally {
            connectRetryCount--;
        }

        if ((connectRetryCount < 0) && (reconnecting)) { // reconnection could not happen while all reconnection
                                                         // attempts are exhausted
            eReceived = new SQLServerException(SQLServerException.getErrString("R_crClientAllRecoveryAttemptsFailed"),
                    eReceived);
        }

        reconnecting = false;
        if (stopRequest) {
            synchronized (stopReconSync) {
                stopReconSync.notify();
            }
        }
        synchronized (reconnectSync) {
            reconnectSync.notify();
        }
        return;
    }

    boolean isRunning() {
        return reconnecting;
    }

    private boolean isFatalError(SQLServerException e) {
        // NOTE: If these conditions are modified, consider modification to conditions in SQLServerConnection::login()
        // and
        // Reconnect::run()
        if ((SQLServerException.LOGON_FAILED == e.getErrorCode()) // actual logon failed, i.e. bad password
                || (SQLServerException.PASSWORD_EXPIRED == e.getErrorCode()) // actual logon failed, i.e. password
                                                                             // isExpired
                || (SQLServerException.DRIVER_ERROR_INVALID_TDS == e.getDriverErrorCode()) // invalid TDS received from
                                                                                           // server
                || (SQLServerException.DRIVER_ERROR_SSL_FAILED == e.getDriverErrorCode()) // failure negotiating SSL
                || (SQLServerException.DRIVER_ERROR_INTERMITTENT_TLS_FAILED == e.getDriverErrorCode()) // failure TLS1.2
                || (SQLServerException.ERROR_SOCKET_TIMEOUT == e.getDriverErrorCode()) // socket timeout ocurred
                || (SQLServerException.DRIVER_ERROR_UNSUPPORTED_CONFIG == e.getDriverErrorCode())) // unsupported
                                                                                                   // configuration
                                                                                                   // (e.g. Sphinx,
                                                                                                   // invalid
                                                                                                   // packet size, etc.)
            return true;
        else
            return false;
    }

    void reset() {
        connectRetryCount = con.getRetryCount();
        eReceived = null;
        stopRequest = false;
    }

    Object getLock() {
        return reconnectSync;
    }

    void stop(boolean blocking) {
        // if (connectionlogger.isLoggable(Level.FINER)) {
        // connectionlogger.finer(this.toString() + "Reconnection stopping");
        // }
        stopRequest = true;

        if (blocking && reconnecting) {
            // If stopRequest is received while reconnecting is true, only then can we receive notify on
            // stopReconnectionObject
            try {
                synchronized (stopReconSync) {
                    if (reconnecting)
                        stopReconSync.wait(); // Wait only if reconnecting is still true. This is to
                                              // avoid a race condition where
                                              // reconnecting set to false and
                                              // stopReconnectionSynchronizer has already notified
                                              // even before following wait() is called.
                }
            } catch (InterruptedException e) {
                // Driver does not generate any interrupts that will generate this exception hence ignoring. This
                // exception should not break
                // current flow of execution hence catching it.
                // if (connectionlogger.isLoggable(Level.FINER)) {
                // connectionlogger.finer(this.toString() + "Interrupt in reconnection stop() is unexpected.");
                // }
            }
        }
    }

    SQLServerException getException() {
        return eReceived;
    }
}
