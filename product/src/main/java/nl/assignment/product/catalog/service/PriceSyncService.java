package nl.assignment.product.catalog.service;

import nl.assignment.product.catalog.domain.Product;
import nl.assignment.product.catalog.integration.ExternalPriceClient;
import nl.assignment.product.catalog.integration.ExternalPriceDto;
import nl.assignment.product.catalog.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.Executors;

/**
 *   AI assisted code
 * Service responsible for synchronizing product prices with an external pricing system.
 *
 * Key characteristics:
 * - Uses virtual threads to perform concurrent price updates efficiently.
 * - Designed for I/O-bound workloads (external API calls).
 * - Runs periodically via @Scheduled.
 * - Delegates actual price updates to ProductService to ensure consistent business logic.
 *
 * This service ensures that the product catalog stays up-to-date with the latest external prices.
 */
@Service
public class PriceSyncService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PriceSyncService.class);

    private final ProductRepository productRepo;
    private final ExternalPriceClient externalClient;
    private final ProductService productService;

    public PriceSyncService(ProductRepository productRepo,
                            ExternalPriceClient externalClient,
                            ProductService productService) {
        this.productRepo = productRepo;
        this.externalClient = externalClient;
        this.productService = productService;
    }

    /**
     * Periodically synchronizes prices for all products.
     *
     * Scheduling:
     * - Runs with a fixed delay defined in configuration:
     *   pricing.sync.interval-ms (default: 60 seconds).
     *
     * Concurrency:
     * - Uses virtual threads (Project Loom) via Executors.newVirtualThreadPerTaskExecutor().
     * - Each product price update runs in its own virtual thread.
     * - Ideal for external API calls because virtual threads are extremely lightweight.
     *
     * Transactionality:
     * Reading products and updating prices occur within a single transaction.
     *
     * Behavior:
     * - Fetches all products.
     * - Submits a price-sync task for each product.
     * - Waits for all tasks to complete when the executor closes.
     */
    @Scheduled(fixedDelayString = "${pricing.sync.interval-ms:60000}")
    @Transactional
    public void syncPrices() {
        long start = System.nanoTime();
        List<Product> products = productRepo.findAll();

        try {
            LOGGER.info("Starting price sync for {} products...", products.size());

            // Virtual thread executor: each submitted task runs in its own virtual thread.
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                products.forEach(product ->
                        executor.submit(() -> syncPrice(product))
                );
            }

            long durationNs = System.nanoTime() - start;
            double durationSec = durationNs / 1_000_000_000.0;

            LOGGER.info("Price sync completed in {} seconds", durationSec);

        } catch (Exception e) {
            long durationNs = System.nanoTime() - start;
            double durationSec = durationNs / 1_000_000_000.0;

            LOGGER.error("Price sync failed after {} seconds", durationSec, e);
        }
    }

    /**
     * Synchronizes the price of a single product.
     *
     * - Calls the external pricing API using the product's SKU and current price.
     * - If the external system returns a new price, delegates the update to ProductService.
     * - ProductService ensures consistent update logic and indexing (e.g., Elasticsearch).
     *
     * This method is intentionally small and focused so it can be executed safely
     * inside a virtual thread without blocking platform threads.
     */
    private void syncPrice(Product product) {
        ExternalPriceDto dto = externalClient.getPrice(product.getSku(), product.getPrice());
        if (dto != null && dto.getPrice() != null) {
            productService.updatePrice(product.getSku(), dto.getPrice());
        }
    }
}