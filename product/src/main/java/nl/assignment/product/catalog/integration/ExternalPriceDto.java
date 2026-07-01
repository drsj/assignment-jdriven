package nl.assignment.product.catalog.integration;

import java.math.BigDecimal;

public class ExternalPriceDto {

    private String sku;
    private BigDecimal price;

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
