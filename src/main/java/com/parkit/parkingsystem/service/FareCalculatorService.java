package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.ParkingType;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket is null");
        }

        if (ticket.getInTime() == null || ticket.getOutTime() == null) {
            throw new IllegalArgumentException("In or out time is null");
        }

        if (ticket.getParkingSpot() == null || ticket.getParkingSpot().getParkingType() == null) {
            throw new IllegalArgumentException("Parking type is null");
        }

        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();

        if (outTime < inTime) {
            throw new IllegalArgumentException("Out time provided is before in time");
        }

        double duration = (double) (outTime - inTime) / (1000 * 60 * 60);

        if (duration < 0.5) {
            ticket.setPrice(0);
            return;
        }

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR:
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                break;

            case BIKE:
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                break;

            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }
    }
}