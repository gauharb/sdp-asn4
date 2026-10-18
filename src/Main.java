
import facade.OrderFacade;
import exception.OrderException;
import model.Cart;
import model.Customer;
import model.Order;
import model.Product;
import subsystem.InventoryService;
import subsystem.NotificationService;
import subsystem.PaymentProcessor;
import subsystem.ShippingService;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        InventoryService inventory = new InventoryService();
        PaymentProcessor payments = new PaymentProcessor();
        ShippingService shipping = new ShippingService();
        shipping.blockCountry("Atlantis");

        Product laptop = new Product("LAP-1", "Laptop", new BigDecimal("1000"));
        Product mouse = new Product("MOU-1", "Mouse", new BigDecimal("25"));
        inventory.addStock("LAP-1", 2);
        inventory.addStock("MOU-1", 10);
        payments.registerAccount("tok-rich", new BigDecimal("5000"));
        payments.registerAccount("tok-poor", new BigDecimal("50"));

        OrderFacade shop = new OrderFacade(inventory, payments, shipping, new NotificationService());

        Customer alice = new Customer("Gauhar", "gauhar@mail.com", "1 Abay St, Kazakhstan", "tok-rich");
        Customer bob = new Customer("Aisu", "aisu@mail.com", "2 Main St, Kazakhstan", "tok-poor");
        Customer eve = new Customer("Almaz", "almaz@mail.com", "3 Sea St, Atlantis", "tok-rich");

        System.out.println("== 1. Successful order ==");
        Order order = attempt(shop, new Cart().add(laptop, 1).add(mouse, 2), alice);
        report(inventory, payments);

        System.out.println("\n== 2. Payment declined -> stock must be released ==");
        attempt(shop, new Cart().add(laptop, 1), bob);
        report(inventory, payments);

        System.out.println("\n== 3. Shipping fails -> payment refunded and stock released ==");
        attempt(shop, new Cart().add(mouse, 1), eve);
        report(inventory, payments);

        System.out.println("\n== 4. Not enough stock ==");
        attempt(shop, new Cart().add(laptop, 5), alice);

        System.out.println("\n== 5. Cancel the first order ==");
        try {
            shop.cancelOrder(order.id());
        } catch (OrderException e) {
            System.out.println("  FAILED: " + e.getMessage());
        }
        report(inventory, payments);
    }

    private static Order attempt(OrderFacade shop, Cart cart, Customer customer) {
        try {
            Order order = shop.placeOrder(cart, customer);
            System.out.println("  OK: " + order.id());
            return order;
        } catch (OrderException e) {
            System.out.println("  FAILED: " + e.getMessage());
            return null;
        }
    }

    private static void report(InventoryService inventory, PaymentProcessor payments) {
        System.out.printf("  state -> laptops=%d, mice=%d, gauhar balance=%s, almaz balance=%s%n",
                inventory.available("LAP-1"), inventory.available("MOU-1"),
                payments.balanceOf("tok-rich"), payments.balanceOf("tok-poor"));
    }
}

