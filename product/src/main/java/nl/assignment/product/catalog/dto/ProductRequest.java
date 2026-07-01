package nl.assignment.product.catalog.dto;

import nl.assignment.product.catalog.domain.Product;
import nl.assignment.product.catalog.util.ProductUtil;

import java.math.BigDecimal;

public class ProductRequest {

    private String sku;
    private String name;
    private String description;
    private String brand;
    private String category;
    private BigDecimal price;
    private Long quantity;
    private String currency;

    public Product toEntity() {
        return ProductUtil.getProduct(sku, name, description, brand, category, price, quantity);
    }

    // Getters/setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Long getQuantity() {
        return quantity;
    }
    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}

