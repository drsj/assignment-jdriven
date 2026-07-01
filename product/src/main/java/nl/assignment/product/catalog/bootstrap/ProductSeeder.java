package nl.assignment.product.catalog.bootstrap;

import nl.assignment.product.catalog.domain.Product;
import nl.assignment.product.catalog.repository.ProductRepository;
import nl.assignment.product.catalog.util.ProductUtil;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * AI generated code
 *
 * Seeds the database with 1000 realistic demo products on application startup.
 *
 * <p>Runs once via {@link CommandLineRunner}. If products already exist the seeding
 * step is skipped, making this safe to run in any environment without creating
 * duplicate data.
 *
 * <p>Each generated product is assigned a random brand, category, price and
 * quantity. The SKU is derived from the brand and product name
 * (e.g. {@code APL-IPH-1}).
 */
@Component
public class ProductSeeder implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductSeeder.class);

    private final ProductRepository productRepository;

    public ProductSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String @NonNull ... args) {
        long count = productRepository.count();

        if (count > 0) {
            LOGGER.info("Skipping product seeding: {} products already exist", count);
            return;
        }

        LOGGER.info("Seeding 1000 products...");
        List<Product> products = new ArrayList<>(1000);

        for (int i = 1; i <= 1000; i++) {
            products.add(generateRealisticProduct(i));
        }

        productRepository.saveAll(products);
        LOGGER.info("Successfully seeded {} products", products.size());
    }

    private Product generateRealisticProduct(int index) {

        String[] brands = {
                "Apple", "Samsung", "Sony", "LG", "Dell",
                "HP", "Lenovo", "Bose", "JBL", "Microsoft"
        };

        String[] categories = {
                "mobile", "laptop", "audio", "tv",
                "tablet", "smartwatch", "speaker", "monitor"
        };

        String brand = brands[ThreadLocalRandom.current().nextInt(brands.length)];
        String category = categories[ThreadLocalRandom.current().nextInt(categories.length)];

        String name = generateName(brand, category);
        String description = brand + " " + name;

        // SKU zoals in het voorbeeld: APL-IPH-13
        String sku = generateSku(brand, name, index);

        BigDecimal price = BigDecimal.valueOf(
                ThreadLocalRandom.current().nextDouble(50.0, 2500.0)
        ).setScale(2, RoundingMode.HALF_UP);

        long quantity = ThreadLocalRandom.current().nextLong(0, 100);

        return ProductUtil.getProduct(sku, name, description, brand, category, price, quantity);
    }

    private String generateName(String brand, String category) {
        return switch (brand) {
            case "Apple" -> switch (category) {
                case "mobile" -> "iPhone 15";
                case "tablet" -> "iPad Pro 12.9";
                case "laptop" -> "MacBook Air M3";
                case "smartwatch" -> "Apple Watch Series 9";
                default -> brand + " " + category;
            };
            case "Samsung" -> switch (category) {
                case "mobile" -> "Galaxy S24";
                case "tablet" -> "Galaxy Tab S9";
                case "tv" -> "Samsung QLED 55\"";
                case "laptop" -> "Galaxy Book 4";
                default -> brand + " " + category;
            };
            case "Sony" -> switch (category) {
                case "audio" -> "WH‑1000XM5";
                case "tv" -> "Bravia XR 65\"";
                case "speaker" -> "Sony SRS‑XB43";
                default -> brand + " " + category;
            };
            case "LG" -> switch (category) {
                case "tv" -> "LG OLED C3 55\"";
                case "monitor" -> "LG UltraFine 27\"";
                default -> brand + " " + category;
            };
            case "Dell" -> "XPS 13";
            case "HP" -> "Spectre x360";
            case "Lenovo" -> "ThinkPad X1 Carbon";
            case "Bose" -> "QuietComfort Ultra";
            case "JBL" -> "Charge 5";
            case "Microsoft" -> "Surface Laptop 6";
            default -> brand + " " + category;
        };
    }

    private String generateSku(String brand, String name, int index) {
        String brandCode = safeCode(brand);
        String nameCode = safeCode(name);

        return brandCode + "-" + nameCode + "-" + index;
    }

    private String safeCode(String input) {
        if (input == null) {
            return "UNK";
        }

        String cleaned = input.replaceAll("[^A-Za-z]", "");

        if (cleaned.length() >= 3) {
            return cleaned.substring(0, 3).toUpperCase();
        }
        return (cleaned + "XXX").substring(0, 3).toUpperCase();
    }
}