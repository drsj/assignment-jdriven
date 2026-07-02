package nl.assignment.product.catalog.service;

import nl.assignment.product.catalog.domain.Product;
import nl.assignment.product.catalog.dto.ProductResponse;
import nl.assignment.product.catalog.repository.ProductRepository;
import nl.assignment.product.catalog.search.ProductDocument;
import nl.assignment.product.catalog.search.repository.ProductSearchRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository productRepo;
    private ProductSearchRepository searchRepo;
    private ProductService service;
    private ElasticsearchOperations elasticsearchOperations;

    @BeforeEach
    void setup() {
        productRepo = mock(ProductRepository.class);
        searchRepo = mock(ProductSearchRepository.class);
        elasticsearchOperations = mock(ElasticsearchOperations.class);
        service = new ProductService(productRepo, searchRepo, elasticsearchOperations);
    }

    @Test
    void should_create_and_index_product() {
        Product p = new Product();
        p.setId(1L);
        p.setSku("APL-IPH-18");
        p.setName("Testproduct");
        p.setPrice(BigDecimal.TEN);
        p.setQuantity(100L);

        when(productRepo.save(any())).thenReturn(p);

        ProductResponse created = service.create(p);

        assertThat(created.id()).isEqualTo(1L);
        assertThat(created.sku()).isEqualTo("APL-IPH-18");

        ArgumentCaptor<ProductDocument> docCaptor = ArgumentCaptor.forClass(ProductDocument.class);
        verify(elasticsearchOperations).save(docCaptor.capture());
        assertThat(docCaptor.getValue().getSku()).isEqualTo("APL-IPH-18");
    }

    @Test
    void should_update_product() {
        Product existing = new Product();
        existing.setId(1L);
        existing.setSku("APL-IPH-18");
        existing.setName("Old");
        existing.setPrice(BigDecimal.ONE);
        existing.setQuantity(100L);

        Product update = new Product();
        update.setName("New");
        update.setPrice(BigDecimal.TEN);
        update.setQuantity(200L);

        when(productRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepo.save(any())).thenReturn(existing);

        ProductResponse updated = service.update(1L, update);

        assertThat(updated.name()).isEqualTo("New");
        assertThat(updated.price()).isEqualTo(BigDecimal.TEN);
        assertThat(updated.quantity()).isEqualTo(200L);
        verify(elasticsearchOperations).save(ArgumentMatchers.<Object>any());
    }

    @Test
    void should_throw_exception_for_unknown_product() {
        when(productRepo.findBySku("ABC")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBySku("ABC"))
                .isInstanceOf(EntityNotFoundException.class);
    }
    
    @Test
    void should_delete_product_from_database_and_elasticsearch() {
        Product product = new Product();
        product.setId(1001L);
        product.setSku("ABC123");

        when(productRepo.findBySku("ABC123")).thenReturn(Optional.of(product));
        service.deleteProduct("ABC123");

        verify(productRepo).delete(product);
        verify(elasticsearchOperations).delete("1001", ProductDocument.class);
    }

    @Test
    void should_throw_exception_when_product_not_found() {
        when(productRepo.findBySku("ABC")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteProduct("ABC"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void should_search_products() {
        SearchHits<ProductDocument> hits = mock(SearchHits.class);
        when(hits.stream()).thenReturn(Stream.empty());
        when(hits.getTotalHits()).thenReturn(0L);
        when(elasticsearchOperations.search(any(Query.class), eq(ProductDocument.class)))
                .thenReturn(hits);

        var result = service.search("test", Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
    }
}