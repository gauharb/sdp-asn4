package model;

import java.math.BigDecimal;

public record Order(String id, Customer customer, Cart cart, BigDecimal total,
                    String reservationId, String paymentId, String shipmentId,
                    OrderStatus status) {

    public Order withStatus(OrderStatus newStatus) {
        return new Order(id, customer, cart, total, reservationId, paymentId, shipmentId, newStatus);
    }
}
