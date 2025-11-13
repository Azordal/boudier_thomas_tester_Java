package com.parkit.parkingsystem;

import com.parkit.parkingsystem.service.InteractiveShell;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

// Tests unitaires de la classe InteractiveShell
public class InteractiveShellTest {

    @Mock
    private InputReaderUtil inputReaderUtil;

    @Mock
    private ParkingService parkingService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoadMenuPrintsAllOptions() throws Exception {
        // Capture la sortie système
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            // Appel de la méthode privée static loadMenu() via réflexion
            Method loadMenu = InteractiveShell.class.getDeclaredMethod("loadMenu");
            loadMenu.setAccessible(true);
            loadMenu.invoke(null);

            String output = outContent.toString();

            assertTrue(output.contains("Please select an option. Simply enter the number to choose an action"));
            assertTrue(output.contains("1 New Vehicle Entering - Allocate Parking Space"));
            assertTrue(output.contains("2 Vehicle Exiting - Generate Ticket Price"));
            assertTrue(output.contains("3 Shutdown System"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testRunMainLoop_Option1ThenExit() {
        when(inputReaderUtil.readSelection()).thenReturn(1, 3);

        InteractiveShell.runMainLoop(inputReaderUtil, parkingService);

        verify(parkingService, times(1)).processIncomingVehicle();
        verify(parkingService, never()).processExitingVehicle();
    }

    @Test
    public void testRunMainLoop_Option2ThenExit() {
        when(inputReaderUtil.readSelection()).thenReturn(2, 3);

        InteractiveShell.runMainLoop(inputReaderUtil, parkingService);

        verify(parkingService, times(1)).processExitingVehicle();
        verify(parkingService, never()).processIncomingVehicle();
    }

    @Test
    public void testRunMainLoop_UnsupportedOptionThenExit() {
        // 4 = option invalide, puis 3 pour quitter
        when(inputReaderUtil.readSelection()).thenReturn(4, 3);

        // Capture la sortie pour vérifier le message "Unsupported option..."
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            InteractiveShell.runMainLoop(inputReaderUtil, parkingService);
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        assertTrue(output.contains("Unsupported option. Please enter a number corresponding to the provided menu"));

        // On vérifie que rien n'a été appelé sur ParkingService
        verify(parkingService, never()).processIncomingVehicle();
        verify(parkingService, never()).processExitingVehicle();
    }
}
