# Order Processing Facade 

Assignment 4 — Facade design pattern 
Option B: Online Order Processin

`OrderFacade` gives clients two simple methods, `placeOrder(cart, customer)` and `cancelOrder(orderId)`.
Behind them it coordinates four subsystem services in the correct order and rolls back
completed steps if something fails (e.g. reserved stock is released when payment is declined).

## Project structure
```
src/
  Main.java        demo client
  exception/       OrderException, OutOfStockException, PaymentDeclinedException, ShippingException
  facade/          OrderFacade
  model/           Product, Cart, CartItem, Customer, Order, OrderStatus
  subsystem/       InventoryService, PaymentProcessor, ShippingService, NotificationService
docs/              UML diagram (uml.png, uml.dot) and report
```

## How it works
`placeOrder`: reserve stock -> charge payment -> schedule shipping -> send confirmation e-mail.
If any step fails, earlier steps are undone in reverse order and the client receives a single
`OrderException`. `cancelOrder` cancels the shipment, refunds the payment, releases the stock
and notifies the customer.

## Run
Requires JDK 17 or newer.
```
javac -d out $(find src -name "*.java")
java -cp out Main
```
Or open the project in IntelliJ IDEA and run `Main`.

The demo shows five scenarios: successful order, declined payment, shipping failure,
out-of-stock, and order cancellation.

## Design notes
- Subsystem classes do not know the Facade exists and can still be used directly.
- The Facade coordinates only; business logic stays in the subsystem classes.
- Subsystem exceptions are translated into `OrderException`, so clients depend only on the Facade.
- Services are injected through the Facade constructor.