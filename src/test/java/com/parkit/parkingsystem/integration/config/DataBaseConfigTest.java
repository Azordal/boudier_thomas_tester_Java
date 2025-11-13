package com.parkit.parkingsystem.config;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

public class DataBaseConfigTest {

    private final DataBaseConfig dataBaseConfig = new DataBaseConfig();

    // ---------- closeConnection ----------

    @Test
    public void closeConnection_withNull_doesNothing() {
        assertDoesNotThrow(() -> dataBaseConfig.closeConnection(null));
    }

    @Test
    public void closeConnection_withValidConnection_closesIt() throws SQLException {
        Connection con = mock(Connection.class);

        assertDoesNotThrow(() -> dataBaseConfig.closeConnection(con));

        verify(con, times(1)).close();
    }

    @Test
    public void closeConnection_whenCloseThrowsException_loggedAndNoCrash() throws SQLException {
        Connection con = mock(Connection.class);
        doThrow(new SQLException("test")).when(con).close();

        assertDoesNotThrow(() -> dataBaseConfig.closeConnection(con));

        verify(con, times(1)).close();
    }

    // ---------- closePreparedStatement ----------

    @Test
    public void closePreparedStatement_withNull_doesNothing() {
        assertDoesNotThrow(() -> dataBaseConfig.closePreparedStatement(null));
    }

    @Test
    public void closePreparedStatement_withValidPs_closesIt() throws SQLException {
        PreparedStatement ps = mock(PreparedStatement.class);

        assertDoesNotThrow(() -> dataBaseConfig.closePreparedStatement(ps));

        verify(ps, times(1)).close();
    }

    @Test
    public void closePreparedStatement_whenCloseThrowsException_loggedAndNoCrash() throws SQLException {
        PreparedStatement ps = mock(PreparedStatement.class);
        doThrow(new SQLException("test")).when(ps).close();

        assertDoesNotThrow(() -> dataBaseConfig.closePreparedStatement(ps));

        verify(ps, times(1)).close();
    }

    // ---------- closeResultSet ----------

    @Test
    public void closeResultSet_withNull_doesNothing() {
        assertDoesNotThrow(() -> dataBaseConfig.closeResultSet(null));
    }

    @Test
    public void closeResultSet_withValidRs_closesIt() throws SQLException {
        ResultSet rs = mock(ResultSet.class);

        assertDoesNotThrow(() -> dataBaseConfig.closeResultSet(rs));

        verify(rs, times(1)).close();
    }

    @Test
    public void closeResultSet_whenCloseThrowsException_loggedAndNoCrash() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        doThrow(new SQLException("test")).when(rs).close();

        assertDoesNotThrow(() -> dataBaseConfig.closeResultSet(rs));

        verify(rs, times(1)).close();
    }
}
