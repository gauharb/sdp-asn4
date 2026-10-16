package model;

import java.math.BigDecimal;

public record CartItem(Product product, int quantity) {
    public CartItem {
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
    }

    public BigDecimal lineTotal() {
        return product.price().multiply(BigDecimal.valueOf(quantity));
    }
}