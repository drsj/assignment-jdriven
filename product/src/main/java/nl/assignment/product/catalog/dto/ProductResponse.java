package nl.assignment.product.catalog.dto;

import nl.assignment.product.catalog.domain.Product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        String brand,
        String category,
        BigDecimal price,
        long quantity,
        String currency,
        Instant lastSyncedAt
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getSku(),
                p.getName(),
                p.getDescription(),
                p.getBrand(),
                p.getCategory(),
                p.getPrice(),
                p.getQuantity(),
                p.getCurrency(),
                p.getLastSyncedAt()
        );
    }
}