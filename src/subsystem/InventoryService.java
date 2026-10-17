package subsystem;

import exception.OutOfStockException;

import model.CartItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InventoryService {
    private final Map<String, Integer> stock = new HashMap<>();
    private final Map<String, List<CartItem>> reservations = new HashMap<>();

    public void addStock(String sku, int quantity) {
        stock.merge(sku, quantity, Integer::sum);
    }

    public int available(String sku) {
        return stock.getOrDefault(sku, 0);
    }

    public String reserve(List<CartItem> items) throws OutOfStockException {
        for (CartItem item : items) {
            if (available(item.product().sku()) < item.quantity()) {
                throw new OutOfStockException("Not enough stock for " + item.product().name());
            }
        }
        items.forEach(i -> stock.merge(i.product().sku(), -i.quantity(), Integer::sum));
        String reservationId = "RES-" + UUID.randomUUID().toString().substring(0, 8);
        reservations.put(reservationId, List.copyOf(items));
        return reservationId;
    }

    public void release(String reservationId) {
        List<CartItem> held = reservations.remove(reservationId);
        if (held != null) {
            held.forEach(i -> stock.merge(i.product().sku(), i.quantity(), Integer::sum));
        }
    }
}
