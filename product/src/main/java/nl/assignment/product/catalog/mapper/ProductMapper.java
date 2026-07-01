package nl.assignment.product.catalog.mapper;

import nl.assignment.product.catalog.domain.Product;
import nl.assignment.product.catalog.dto.ProductResponse;
import nl.assignment.product.catalog.dto.ProductSearchResponse;
import nl.assignment.product.catalog.search.ProductDocument;

public final class ProductMapper {

    private ProductMapper() {}

    public static ProductResponse toResponse(Product p) {
        return ProductResponse.from(p);
    }

    public static ProductSearchResponse toSearchResponse(ProductDocument doc) {
        return ProductSearchResponse.from(doc);
    }
}