package com.petshop.model;

import java.math.BigDecimal;

public record InventoryItem(
        String productId,
        String productName,
        String barcode,
        int quantityOnHand,
        String unit,
        int minimumQuantity,
        BigDecimal salePrice,
        String supplierName) {
    public boolean isBelowMinimum() {
        return quantityOnHand <= minimumQuantity;
    }
}
