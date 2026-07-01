package nl.assignment.product.catalog.util;

import nl.assignment.product.catalog.domain.Product;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;

public class ProductUtil {

    private ProductUtil() {

    }

    public static @NonNull Product getProduct(String sku, String name, String description, String brand, String category, BigDecimal price, long quantity) {
        Product p = new Product();
        p.setSku(sku);
        p.setName(name);
        p.setDescription(description);
        p.setBrand(brand);
        p.setCategory(category);
        p.setPrice(price);
        p.setQuantity(quantity);
        p.setCurrency("EUR");
        return p;
    }
}
