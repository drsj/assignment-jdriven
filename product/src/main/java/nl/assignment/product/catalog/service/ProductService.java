package nl.assignment.product.catalog.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import nl.assignment.product.catalog.domain.Product;
import nl.assignment.product.catalog.dto.ProductResponse;
import nl.assignment.product.catalog.dto.ProductSearchResponse;
import nl.assignment.product.catalog.exception.QuantityUpdateException;
import nl.assignment.product.catalog.mapper.ProductMapper;
import nl.assignment.product.catalog.repository.ProductRepository;
import nl.assignment.product.catalog.search.ProductDocument;
import nl.assignment.product.catalog.search.repository.ProductSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
/*
 *   AI assisted code
 */
public class ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepo;
    private final ProductSearchRepository searchRepo;
    private final ElasticsearchOperations elasticsearchOperations;

    public ProductService(ProductRepository productRepo,
                          ProductSearchRepository searchRepo,
                          ElasticsearchOperations elasticsearchOperations) {
        this.productRepo = productRepo;
        this.searchRepo = searchRepo;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /**
     * Reindex all products into Elasticsearch on startup.
     *
     * This ensures that the search index is in sync with the database
     * even if data was inserted before Elasticsearch was enabled.
     */
    @PostConstruct
    public void reindexAll() {
        productRepo.findAll().forEach(this::index);
    }

    /**
     * Create a new product.
     *
     * - Saves the Product entity in PostgreSQL.
     * - Indexes the product in Elasticsearch.
     * - Returns a ProductResponse DTO.
     */
    @Transactional
    public ProductResponse create(Product product) {
        Product saved = productRepo.save(product);
        index(saved);
        return ProductMapper.toResponse(saved);
    }

    /**
     * Fully update an existing product.
     *
     * - Loads the existing entity.
     * - Applies updates.
     * - Saves the updated entity.
     * - Reindexes the updated product.
     * - Returns a ProductResponse DTO.
     */
    @Transactional
    public ProductResponse update(Long id, Product update) {
        Product existing = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        existing.setName(update.getName());
        existing.setDescription(update.getDescription());
        existing.setBrand(update.getBrand());
        existing.setCategory(update.getCategory());
        existing.setPrice(update.getPrice());
        existing.setQuantity(update.getQuantity());
        existing.setCurrency(update.getCurrency());

        Product saved = productRepo.save(existing);
        index(saved);
        return ProductMapper.toResponse(saved);
    }

    /**
     * Update the price of a product.
     *
     * - Used by the scheduled price sync job.
     * - Updates the entity and reindexes it.
     */
    public void updatePrice(String sku, BigDecimal newPrice) {
        Product product = findProductOrThrow(sku);
        product.setPrice(newPrice);
        product.setLastSyncedAt(Instant.now());
        productRepo.save(product);
        index(product);
    }

    /**
     * Update the quantity of a product.
     *
     * - Validates stock changes.
     * - Prevents negative stock.
     * - Saves the updated entity.
     */
    @Transactional
    public void updateQuantity(String sku, long delta) {
        Product product = findProductOrThrow(sku);

        long updated = product.getQuantity() + delta;

        if (updated < 0) {
            throw new QuantityUpdateException("Cannot decrease quantity below zero");
        }

        product.setQuantity(updated);
        productRepo.save(product);
    }
    
    /**
     * Deletes a product by its SKU from both PostgreSQL and Elasticsearch.
     */
    @Transactional
    public void deleteProduct(String sku) {
        Product product = findProductOrThrow(sku);
        productRepo.delete(product);
        elasticsearchOperations.delete(product.getId().toString(), ProductDocument.class);
    }

    /**
     * Retrieve a product by SKU.
     *
     * - Returns a ProductResponse DTO.
     */
    public ProductResponse getBySku(String sku) {
        Product product = findProductOrThrow(sku);
        return ProductMapper.toResponse(product);
    }

    /**
     * Retrieve a paginated list of products.
     *
     * - Returns Page<ProductResponse>
     */
    public Page<ProductResponse> list(Pageable pageable) {
        return productRepo.findAll(pageable)
                .map(ProductMapper::toResponse);
    }

    /**
     * Searches products in Elasticsearch using a multi-strategy boolean query.
     *
     * The query combines:
     * - multi_match with fuzziness
     * - match_phrase
     * - prefix search
     *
     * - Returns Page<ProductSearchResponse>
     */
    public Page<ProductSearchResponse> search(String query, Pageable pageable) {

        Query esQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .should(s -> s.multiMatch(m -> m
                                .query(query)
                                .fields("name^5", "category^3", "tags^2", "description")
                                .fuzziness("AUTO")
                        ))
                        .should(s -> s.matchPhrase(mp -> mp
                                .field("description")
                                .query(query)
                        ))
                        .should(s -> s.prefix(p -> p
                                .field("name")
                                .value(query)
                        ))
                ))
                .withPageable(pageable)
                .build();

        SearchHits<ProductDocument> hits =
                elasticsearchOperations.search(esQuery, ProductDocument.class);

        List<ProductSearchResponse> content = hits
                .stream()
                .map(SearchHit::getContent)
                .map(ProductMapper::toSearchResponse)
                .toList();

        return new PageImpl<>(content, pageable, hits.getTotalHits());
    }

    /**
     * Helper method to load a product or throw an exception.
     */
    private Product findProductOrThrow(String sku) {
        return productRepo.findBySku(sku)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }

    /**
     * Index a product into Elasticsearch.
     *
     * - Converts the Product entity into a ProductDocument.
     * - Saves the document using the Elasticsearch repository.
     */
    private void index(Product product) {
        ProductDocument doc = new ProductDocument();
        doc.setId(product.getId());
        doc.setSku(product.getSku());
        doc.setName(product.getName());
        doc.setDescription(product.getDescription());
        doc.setBrand(product.getBrand());
        doc.setCategory(product.getCategory());
        doc.setPrice(product.getPrice());
        doc.setQuantity(product.getQuantity());
        doc.setCurrency(product.getCurrency());
        elasticsearchOperations.save(doc);
    }
}