package nl.assignment.product.catalog.search.repository;

import nl.assignment.product.catalog.search.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {

    @Query("{\"multi_match\":{\"query\":\"?0\",\"fields\":[\"name^3\",\"description\",\"brand\",\"category\",\"sku\"]}}")
    Page<ProductDocument> search(String query, Pageable pageable);
}