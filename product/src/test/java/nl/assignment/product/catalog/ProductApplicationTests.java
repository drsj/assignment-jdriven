package nl.assignment.product.catalog;

import nl.assignment.product.catalog.search.repository.ProductSearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest
class ProductApplicationTests {

    @MockitoBean
    private ProductSearchRepository productSearchRepository;

    @Test
    void contextLoads() {
    }

}
