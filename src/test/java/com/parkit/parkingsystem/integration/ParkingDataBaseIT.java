package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        // Tous les tests utiliseront une voiture sur la place 1 avec immatriculation ABCDEF
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {
        // rien de spécial ici, la DB de test est déjà nettoyée à chaque test
    }

    @Test
    public void testParkingACar() {
        // GIVEN
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // WHEN : on fait entrer un véhicule
        parkingService.processIncomingVehicle();

        // THEN : un ticket est bien enregistré en base
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket, "Le ticket doit exister en base");
        assertEquals("ABCDEF", ticket.getVehicleRegNumber());
        assertNotNull(ticket.getInTime(), "L'heure d'entrée doit être renseignée");
        assertNull(ticket.getOutTime(), "L'heure de sortie doit être null tant que le véhicule est garé");

        // Et la place utilisée n’est plus disponible
        int nextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(ticket.getParkingSpot().getParkingType());
        assertNotEquals(ticket.getParkingSpot().getId(), nextAvailableSlot,
                "La place utilisée ne doit plus être la prochaine place disponible");
    }

    @Test
    public void testParkingLotExit() {
        // GIVEN
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // On fait entrer puis sortir le véhicule
        parkingService.processIncomingVehicle();
        parkingService.processExitingVehicle();

        // THEN : le ticket en base doit avoir une heure de sortie renseignée
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket, "Le ticket doit exister en base");
        assertNotNull(ticket.getOutTime(), "L'heure de sortie doit être renseignée");
        // Le prix est calculé (il peut être 0 à cause de la règle des 30 minutes gratuites, mais jamais négatif)
        assertTrue(ticket.getPrice() >= 0, "Le prix doit être supérieur ou égal à 0");

        // La place doit à nouveau être disponible
        int nextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(ticket.getParkingSpot().getParkingType());
        assertEquals(ticket.getParkingSpot().getId(), nextAvailableSlot,
                "Après la sortie, la place doit redevenir disponible");
    }

    @Test
    public void testParkingLotExitRecurringUser() {
        // GIVEN
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // Première visite : entrée + sortie (crée un premier ticket pour ABCDEF)
        parkingService.processIncomingVehicle();
        parkingService.processExitingVehicle();

        // Deuxième visite : entrée + sortie (l'utilisateur est maintenant récurrent)
        parkingService.processIncomingVehicle();
        parkingService.processExitingVehicle();

        // THEN : il doit y avoir au moins 2 tickets en base pour cet utilisateur
        int nbTickets = ticketDAO.getNbTicket("ABCDEF");
        assertTrue(nbTickets >= 2, "L'utilisateur doit être considéré comme récurrent (au moins 2 tickets)");

        // Le dernier ticket lu doit avoir une heure de sortie renseignée et un prix calculé
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket, "Le ticket doit exister en base");
        assertNotNull(ticket.getOutTime(), "L'heure de sortie doit être renseignée");
        assertTrue(ticket.getPrice() >= 0, "Le prix doit être supérieur ou égal à 0 (avec remise éventuelle)");
    }
}