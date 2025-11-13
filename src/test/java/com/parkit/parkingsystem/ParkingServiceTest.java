package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ParkingServiceTest {

    private ParkingService parkingService;

    @Mock
    private InputReaderUtil inputReaderUtil;

    @Mock
    private ParkingSpotDAO parkingSpotDAO;

    @Mock
    private TicketDAO ticketDAO;

    @BeforeEach
    public void setUpPerTest() {
        MockitoAnnotations.initMocks(this);
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

    /**
     * Cas 1 : tout se passe bien à l’entrée.
     */
    @Test
    public void testProcessIncomingVehicle() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1); // CAR
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(1);
        when(parkingSpotDAO.updateParking(any())).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(0); // pas encore fidèle

        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
    }

    /**
     * Cas 2 : processIncomingVehicle mais l’utilisateur est fidèle (nbTicket > 0).
     * Permet de couvrir la branche du message de fidélité.
     */
    @Test
    public void testProcessIncomingVehicleRegularUser() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1); // CAR
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(1);
        when(parkingSpotDAO.updateParking(any())).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(3); // fidèle

        parkingService.processIncomingVehicle();

        verify(ticketDAO, times(1)).getNbTicket("ABCDEF");
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
    }

    /**
     * Cas 3 : getNextParkingNumberIfAvailable retourne un spot valide.
     */
    @Test
    public void testGetNextParkingNumberIfAvailable() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1); // CAR
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        assertNotNull(parkingSpot);
        assertEquals(1, parkingSpot.getId());
        assertEquals(ParkingType.CAR, parkingSpot.getParkingType());
        assertTrue(parkingSpot.isAvailable());
    }

    /**
     * Cas 4 : aucun emplacement dispo -> méthode renvoie null.
     */
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1); // CAR
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        assertNull(parkingSpot);
    }

    /**
     * Cas 5 : mauvais type de véhicule (saisie 3) -> null et IllegalArgument catched.
     */
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        when(inputReaderUtil.readSelection()).thenReturn(3); // mauvais choix

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        assertNull(parkingSpot);
    }

    /**
     * Cas 6 : sortie normale, ticket mis à jour, place libérée.
     */
    @Test
    public void processExitingVehicleTest() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setInTime(new Date(System.currentTimeMillis() - 60 * 60 * 1000)); // 1h
        ticket.setOutTime(new Date());

        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2); // fidèle -> remise
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
    }

    /**
     * Cas 7 : updateTicket retourne false -> pas d’erreur levée, mais message d’erreur.
     */
    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setInTime(new Date(System.currentTimeMillis() - 60 * 60 * 1000));
        ticket.setOutTime(new Date());

        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1); // pas de remise
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        assertDoesNotThrow(() -> parkingService.processExitingVehicle());
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
        // pas d’appel obligatoire à updateParking ici
    }

    /**
     * Cas 8 : ticket null renvoyé par DAO -> catch de l’exception et pas de crash.
     */
    @Test
    public void testProcessExitingVehicleWithNullTicket() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket("ABCDEF")).thenReturn(null);

        assertDoesNotThrow(() -> parkingService.processExitingVehicle());
        verify(ticketDAO, times(1)).getTicket("ABCDEF");
    }
}