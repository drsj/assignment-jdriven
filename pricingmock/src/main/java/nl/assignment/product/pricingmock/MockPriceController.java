package nl.assignment.product.pricingmock;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mock external pricing service.
 * Returns a price adjusted by +/- 1-10% based on the input price.
 * In a real scenario this would be a separate system.
 */
@RestController
@RequestMapping("/mock-prices")
public class MockPriceController {

    @GetMapping(value = "/{sku}", produces = MediaType.APPLICATION_JSON_VALUE)
    public MockPriceResponse getPrice(@PathVariable String sku,
                                      @RequestParam(name = "basePrice", required = false) BigDecimal basePrice) {

        BigDecimal effectiveBase = basePrice != null ? basePrice : BigDecimal.valueOf(100);

        // Random percentage between 1% and 10%
        double percentage = ThreadLocalRandom.current().nextDouble(0.01, 0.10);
        // Randomly + or -
        boolean increase = ThreadLocalRandom.current().nextBoolean();

        BigDecimal factor = BigDecimal.valueOf(percentage);
        BigDecimal delta = effectiveBase.multiply(factor);
        BigDecimal newPrice = increase
                ? effectiveBase.add(delta)
                : effectiveBase.subtract(delta);

        MockPriceResponse response = new MockPriceResponse();
        response.setSku(sku);
        response.setPrice(newPrice.setScale(2, RoundingMode.HALF_UP));
        return response;
    }

    public static class MockPriceResponse {
        private String sku;
        private BigDecimal price;

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }
}