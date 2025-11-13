package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount) {
        if (ticket.getOutTime() == null || ticket.getOutTime().before(ticket.getInTime())) {
            throw new IllegalArgumentException(
                    "Out time provided is incorrect: " + ticket.getOutTime()
            );
        }

        long inMillis = ticket.getInTime().getTime();
        long outMillis = ticket.getOutTime().getTime();

        double duration = (outMillis - inMillis) / (1000.0 * 60 * 60);

        // Moins de 30 minutes : gratuit
        if (duration <= 0.5) {
            ticket.setPrice(0);
            return;
        }

        if (ticket.getParkingSpot() == null || ticket.getParkingSpot().getParkingType() == null) {
            throw new IllegalArgumentException("Unknown Parking Type");
        }

        ParkingType parkingType = ticket.getParkingSpot().getParkingType();
        double price;

        switch (parkingType) {
            case CAR:
                price = duration * Fare.CAR_RATE_PER_HOUR;
                break;
            case BIKE:
                price = duration * Fare.BIKE_RATE_PER_HOUR;
                break;
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }

        if (discount) {
            price *= 0.95;
        }

        ticket.setPrice(price);
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}