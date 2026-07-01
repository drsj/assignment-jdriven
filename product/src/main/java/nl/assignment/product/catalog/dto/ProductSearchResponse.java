package nl.assignment.product.catalog.dto;

import nl.assignment.product.catalog.search.ProductDocument;

import java.math.BigDecimal;

public record ProductSearchResponse(
        Long id,
        String sku,
        String name,
        String description,
        String brand,
        String category,
        BigDecimal price,
        long quantity,
        String currency
) {
    public static ProductSearchResponse from(ProductDocument doc) {
        return new ProductSearchResponse(
                doc.getId(),
                doc.getSku(),
                doc.getName(),
                doc.getDescription(),
                doc.getBrand(),
                doc.getCategory(),
                doc.getPrice(),
                doc.getQuantity(),
                doc.getCurrency()
        );
    }
}