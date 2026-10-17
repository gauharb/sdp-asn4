package subsystem;

import exception.ShippingException;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ShippingService {
    private final Set<String> undeliverableCountries = new HashSet<>();
    private final Set<String> activeShipments = new HashSet<>();

    public void blockCountry(String country) {
        undeliverableCountries.add(country.toLowerCase());
    }

    public boolean isActive(String shipmentId) {
        return activeShipments.contains(shipmentId);
    }

    public String schedule(String address) throws ShippingException {
        String country = address.substring(address.lastIndexOf(',') + 1).trim().toLowerCase();
        if (undeliverableCountries.contains(country)) {
            throw new ShippingException("We cannot ship to " + country);
        }
        String shipmentId = "SHP-" + UUID.randomUUID().toString().substring(0, 8);
        activeShipments.add(shipmentId);
        return shipmentId;
    }

    public void cancel(String shipmentId) {
        activeShipments.remove(shipmentId);
    }
}

