package nl.assignment.product.catalog.dto;

import java.math.BigDecimal;

public class PriceUpdateRequest {

    private String sku;
    private BigDecimal price;

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}

