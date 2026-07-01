package nl.assignment.product.catalog.service;


import nl.assignment.product.catalog.domain.Product;
import nl.assignment.product.catalog.integration.ExternalPriceClient;
import nl.assignment.product.catalog.integration.ExternalPriceDto;
import nl.assignment.product.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/*
 *   AI assisted code
 */
class PriceSyncServiceTest {

    private ProductRepository productRepo;
    private ExternalPriceClient externalClient;
    private ProductService productService;
    private PriceSyncService syncService;

    @BeforeEach
    void setup() {
        productRepo = mock(ProductRepository.class);
        externalClient = mock(ExternalPriceClient.class);
        productService = mock(ProductService.class);
        syncService = new PriceSyncService(productRepo, externalClient, productService);
    }

    @Test
    void should_be_able_to_sync_prices() {
        Product p = new Product();
        p.setId(1L);
        p.setSku("APL-IPH-18");
        p.setPrice(BigDecimal.valueOf(42));

        when(productRepo.findAll()).thenReturn(List.of(p));

        ExternalPriceDto dto = new ExternalPriceDto();
        dto.setSku("APL-IPH-18");
        dto.setPrice(BigDecimal.valueOf(45));

        when(externalClient.getPrice("APL-IPH-18", BigDecimal.valueOf(42))).thenReturn(dto);

        syncService.syncPrices();
        verify(productService).updatePrice("APL-IPH-18", BigDecimal.valueOf(45));
    }
}
