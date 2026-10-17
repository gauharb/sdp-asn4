package subsystem;

import exception.PaymentDeclinedException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PaymentProcessor {
    private final Map<String, BigDecimal> balances = new HashMap<>();
    private final Map<String, Charge> charges = new HashMap<>();

    private record Charge(String token, BigDecimal amount) { }

    public void registerAccount(String token, BigDecimal balance) {
        balances.put(token, balance);
    }

    public BigDecimal balanceOf(String token) {
        return balances.getOrDefault(token, BigDecimal.ZERO);
    }

    public String charge(String token, BigDecimal amount) throws PaymentDeclinedException {
        if (!balances.containsKey(token)) {
            throw new PaymentDeclinedException("Unknown payment method");
        }
        if (balances.get(token).compareTo(amount) < 0) {
            throw new PaymentDeclinedException("Insufficient funds");
        }
        balances.merge(token, amount.negate(), BigDecimal::add);
        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8);
        charges.put(paymentId, new Charge(token, amount));
        return paymentId;
    }

    public void refund(String paymentId) {
        Charge charge = charges.remove(paymentId);
        if (charge != null) {
            balances.merge(charge.token(), charge.amount(), BigDecimal::add);
        }
    }
}
