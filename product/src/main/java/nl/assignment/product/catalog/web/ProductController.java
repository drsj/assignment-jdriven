package nl.assignment.product.catalog.web;

import nl.assignment.product.catalog.dto.ProductRequest;
import nl.assignment.product.catalog.dto.ProductResponse;
import nl.assignment.product.catalog.dto.ProductSearchResponse;
import nl.assignment.product.catalog.dto.UpdateQuantityRequest;
import nl.assignment.product.catalog.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    /**
     * Create a new product.
     *
     * - Accepts a ProductRequest DTO from the client.
     * - Converts the DTO into a Product entity.
     * - Delegates creation to the ProductService.
     * - Returns HTTP 201 (Created) with the newly created product.
     *
     * After refactoring:
     * - The controller no longer returns Product entities.
     * - The service returns a ProductResponse DTO instead.
     */
    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestBody ProductRequest request) {
        ProductResponse created = service.create(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update only the quantity of an existing product.
     *
     * - Uses PATCH because this is a partial update (only one field changes).
     * - Identifies the product by its SKU (unique product code).
     * - Delegates the update to the ProductService.
     * - Returns HTTP 204 (No Content) when the update succeeds.
     *
     */
    @PatchMapping("/{sku}/quantity")
    public ResponseEntity<Void> updateQuantity(
            @PathVariable String sku,
            @RequestBody UpdateQuantityRequest request) {

        service.updateQuantity(sku, request.quantity());
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Deletes a product identified by its SKU.
     *
     *  Delegates the deletion logic to the ProductService, which ensures both the database record
     *  and the corresponding Elasticsearch document are removed.
     * - Delegates the update to the ProductService.
     * - Returns HTTP 204 (No Content) when the deletion is succeeds.
     * - Returns HTTP 404 (Not Found) when the deletion fails.
     */
    @DeleteMapping("/{sku}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String sku) {
        service.deleteProduct(sku);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieve a single product by its SKU.
     *
     * - SKU is the unique identifier for products.
     * - The service throws an exception if the product does not exist.
     * - Returns a ProductResponse DTO instead of a Product entity.
     *
     * This endpoint is used for detail views or product lookup.
     */
    @GetMapping("/{sku}")
    public ProductResponse get(@PathVariable String sku) {
        return service.getBySku(sku);
    }

    /**
     * Fully update an existing product.
     *
     * - Uses PUT because the entire product representation is replaced.
     * - Identifies the product by its database ID.
     * - Converts the incoming DTO into a Product entity.
     * - Delegates the update to the ProductService.
     */
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @RequestBody ProductRequest request) {
        return service.update(id, request.toEntity());
    }

    /**
     * Retrieve a paginated and optionally sorted list of products.
     *
     * Spring Boot 4.1 no longer auto-binds Pageable parameters,
     * so pagination and sorting are manually constructed.
     *
     * Supported query parameters:
     * - page: page number (default 0)
     * - size: page size (default 20)
     * - sort: "property,direction" (e.g. "name,asc" or "price,desc")
     *
     * Examples:
     *   /api/products?page=1&size=10
     *   /api/products?sort=name,asc
     */
    @GetMapping
    public Page<ProductResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        LOGGER.warn("Getting all products: page={}, size={}, sort={}", page, size, sort);

        Pageable pageable;

        if (sort != null) {
            String[] parts = sort.split(",");
            String property = parts[0].trim();
            String directionString = parts.length > 1 ? parts[1].trim() : "asc";
            Sort.Direction direction = Sort.Direction.fromString(directionString);
            pageable = PageRequest.of(page, size, Sort.by(direction, property));
        } else {
            pageable = PageRequest.of(page, size);
        }

        return service.list(pageable);
    }

    /**
     * Full-text search endpoint backed by Elasticsearch.
     *
     * - Accepts a free-text query parameter "q".
     * - Supports pagination via Pageable.
     * - Delegates to ProductService which builds an Elasticsearch query.
     *
     * This endpoint is intended for:
     * - multi-field search
     * - fuzzy search (typo tolerance)
     * - relevance ranking
     * - prefix search (autocomplete-like behavior)
     * - phrase search
     */
    @GetMapping("/search")
    public Page<ProductSearchResponse> search(@RequestParam("q") String query, Pageable pageable) {
        return service.search(query, pageable);
    }
}