package nl.assignment.product.catalog.integration;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

/**
 * Client for the external (mock) pricing service.
 */
@Component
public class ExternalPriceClient {

    private final WebClient client;

    public ExternalPriceClient(WebClient.Builder builder) {
        this.client = builder.baseUrl("http://pricing-mock:9090").build();
    }

    public ExternalPriceDto getPrice(String sku, BigDecimal currentPrice) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/mock-prices/{sku}")
                        .queryParam("basePrice", currentPrice)
                        .build(sku))
                .retrieve()
                .bodyToMono(ExternalPriceDto.class)
                .block();
    }
}