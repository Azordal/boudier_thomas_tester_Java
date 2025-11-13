package com.parkit.parkingsystem;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TicketDAOTest {

    @Test
    public void saveTicketShouldInsertRowInDatabase() throws Exception {
        TicketDAO ticketDAO = new TicketDAO();

        // on mock tout ce qui touche à la BDD
        DataBaseConfig dataBaseConfig = mock(DataBaseConfig.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        ticketDAO.dataBaseConfig = dataBaseConfig;

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(DBConstants.SAVE_TICKET)).thenReturn(preparedStatement);

        Ticket ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(10.0);
        ticket.setInTime(new Date(System.currentTimeMillis() - 60 * 60 * 1000));
        ticket.setOutTime(new Date());

        boolean result = ticketDAO.saveTicket(ticket);

        assertTrue(result);
        verify(preparedStatement, times(1)).execute();
        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    public void saveTicketShouldReturnFalseWhenExceptionOccurs() throws Exception {
        TicketDAO ticketDAO = new TicketDAO();

        DataBaseConfig dataBaseConfig = mock(DataBaseConfig.class);
        ticketDAO.dataBaseConfig = dataBaseConfig;

        // on simule une erreur de connexion BDD
        when(dataBaseConfig.getConnection()).thenThrow(new SQLException("DB error"));

        Ticket ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(10.0);
        ticket.setInTime(new Date());
        ticket.setOutTime(new Date());

        boolean result = ticketDAO.saveTicket(ticket);

        assertFalse(result);
        verify(dataBaseConfig, times(1)).closeConnection(null);
    }

    @Test
    public void getTicketShouldReturnTicketWhenFound() throws Exception {
        TicketDAO ticketDAO = new TicketDAO();

        DataBaseConfig dataBaseConfig = mock(DataBaseConfig.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        ticketDAO.dataBaseConfig = dataBaseConfig;

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(DBConstants.GET_TICKET)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);
        when(resultSet.getInt(2)).thenReturn(42);
        when(resultSet.getDouble(3)).thenReturn(5.5);
        Timestamp inTime = new Timestamp(System.currentTimeMillis() - 60 * 60 * 1000);
        Timestamp outTime = new Timestamp(System.currentTimeMillis());
        when(resultSet.getTimestamp(4)).thenReturn(inTime);
        when(resultSet.getTimestamp(5)).thenReturn(outTime);
        when(resultSet.getString(6)).thenReturn(ParkingType.CAR.toString());

        String regNumber = "ABCDEF";
        Ticket ticket = ticketDAO.getTicket(regNumber);

        assertNotNull(ticket);
        assertEquals(regNumber, ticket.getVehicleRegNumber());
        assertEquals(42, ticket.getId());
        assertEquals(5.5, ticket.getPrice());
        assertEquals(1, ticket.getParkingSpot().getId());
        assertEquals(ParkingType.CAR, ticket.getParkingSpot().getParkingType());

        verify(dataBaseConfig, times(1)).closeResultSet(resultSet);
        verify(dataBaseConfig, times(1)).closePreparedStatement(preparedStatement);
        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    public void getTicketShouldReturnNullWhenExceptionOccurs() throws Exception {
        TicketDAO ticketDAO = new TicketDAO();

        DataBaseConfig dataBaseConfig = mock(DataBaseConfig.class);
        ticketDAO.dataBaseConfig = dataBaseConfig;

        when(dataBaseConfig.getConnection()).thenThrow(new SQLException("DB error"));

        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        assertNull(ticket);
        verify(dataBaseConfig, times(1)).closeConnection(null);
    }

    @Test
    public void updateTicketShouldUpdatePriceAndOutTime() throws Exception {
        TicketDAO ticketDAO = new TicketDAO();

        DataBaseConfig dataBaseConfig = mock(DataBaseConfig.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        ticketDAO.dataBaseConfig = dataBaseConfig;

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(DBConstants.UPDATE_TICKET)).thenReturn(preparedStatement);

        Ticket ticket = new Ticket();
        ticket.setId(7);
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(12.5);
        ticket.setInTime(new Date(System.currentTimeMillis() - 60 * 60 * 1000));
        ticket.setOutTime(new Date());

        boolean result = ticketDAO.updateTicket(ticket);

        assertTrue(result);
        verify(preparedStatement, times(1)).execute();
        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    public void updateTicketShouldReturnFalseWhenExceptionOccurs() throws Exception {
        TicketDAO ticketDAO = new TicketDAO();

        DataBaseConfig dataBaseConfig = mock(DataBaseConfig.class);
        ticketDAO.dataBaseConfig = dataBaseConfig;

        when(dataBaseConfig.getConnection()).thenThrow(new SQLException("DB error"));

        Ticket ticket = new Ticket();
        ticket.setId(7);
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(12.5);
        ticket.setInTime(new Date());
        ticket.setOutTime(new Date());

        boolean result = ticketDAO.updateTicket(ticket);

        assertFalse(result);
        verify(dataBaseConfig, times(1)).closeConnection(null);
    }

    @Test
    public void getNbTicketShouldReturnNumberOfTicketsForGivenVehicle() throws Exception {
        TicketDAO ticketDAO = new TicketDAO();

        DataBaseConfig dataBaseConfig = mock(DataBaseConfig.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        ticketDAO.dataBaseConfig = dataBaseConfig;

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT COUNT(*) FROM ticket WHERE VEHICLE_REG_NUMBER=?"))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(3);

        int count = ticketDAO.getNbTicket("ABCDEF");

        assertEquals(3, count);
        verify(dataBaseConfig, times(1)).closeResultSet(resultSet);
        verify(dataBaseConfig, times(1)).closePreparedStatement(preparedStatement);
        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    public void getNbTicketShouldReturnZeroWhenExceptionOccurs() throws Exception {
        TicketDAO ticketDAO = new TicketDAO();

        DataBaseConfig dataBaseConfig = mock(DataBaseConfig.class);
        ticketDAO.dataBaseConfig = dataBaseConfig;

        when(dataBaseConfig.getConnection()).thenThrow(new SQLException("DB error"));

        int count = ticketDAO.getNbTicket("ABCDEF");

        assertEquals(0, count);
        verify(dataBaseConfig, times(1)).closeConnection(null);
    }
}