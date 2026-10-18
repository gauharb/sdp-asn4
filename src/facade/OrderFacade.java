package facade;

import exception.OrderException;
import model.Cart;
import model.Customer;
import model.Order;
import model.OrderStatus;
import exception.OutOfStockException;
import subsystem.InventoryService;
import subsystem.NotificationService;
import exception.PaymentDeclinedException;
import subsystem.PaymentProcessor;
import exception.ShippingException;
import subsystem.ShippingService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OrderFacade {
    private final InventoryService inventory;
    private final PaymentProcessor payments;
    private final ShippingService shipping;
    private final NotificationService notifications;
    private final Map<String, Order> orders = new HashMap<>();

    public OrderFacade(InventoryService inventory, PaymentProcessor payments,
                       ShippingService shipping, NotificationService notifications) {
        this.inventory = inventory;
        this.payments = payments;
        this.shipping = shipping;
        this.notifications = notifications;
    }

    public Order placeOrder(Cart cart, Customer customer) throws OrderException {
        if (cart.isEmpty()) {
            throw new OrderException("Cart is empty");
        }

        String reservationId = reserveStock(cart);
        String paymentId = null;
        try {
            paymentId = charge(cart, customer);
            String shipmentId = ship(customer);

            Order order = new Order("ORD-" + UUID.randomUUID().toString().substring(0, 8),
                    customer, cart, cart.total(), reservationId, paymentId, shipmentId,
                    OrderStatus.CONFIRMED);
            orders.put(order.id(), order);
            notifications.send(customer.email(), "Order confirmed",
                    order.id() + " total " + order.total());
            return order;
        } catch (OrderException e) {
            rollback(reservationId, paymentId);
            throw e;
        }
    }

    public Order cancelOrder(String orderId) throws OrderException {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new OrderException("Unknown order " + orderId);
        }
        if (order.status() == OrderStatus.CANCELLED) {
            throw new OrderException("Order " + orderId + " is already cancelled");
        }
        shipping.cancel(order.shipmentId());
        payments.refund(order.paymentId());
        inventory.release(order.reservationId());

        Order cancelled = order.withStatus(OrderStatus.CANCELLED);
        orders.put(orderId, cancelled);
        notifications.send(order.customer().email(), "Order cancelled", orderId + " was refunded");
        return cancelled;
    }

    private String reserveStock(Cart cart) throws OrderException {
        try {
            return inventory.reserve(cart.items());
        } catch (OutOfStockException e) {
            throw new OrderException(e.getMessage(), e);
        }
    }

    private String charge(Cart cart, Customer customer) throws OrderException {
        try {
            return payments.charge(customer.paymentToken(), cart.total());
        } catch (PaymentDeclinedException e) {
            throw new OrderException("Payment declined: " + e.getMessage(), e);
        }
    }

    private String ship(Customer customer) throws OrderException {
        try {
            return shipping.schedule(customer.address());
        } catch (ShippingException e) {
            throw new OrderException("Shipping failed: " + e.getMessage(), e);
        }
    }

    private void rollback(String reservationId, String paymentId) {
        if (paymentId != null) {
            payments.refund(paymentId);
        }
        inventory.release(reservationId);
    }
}
