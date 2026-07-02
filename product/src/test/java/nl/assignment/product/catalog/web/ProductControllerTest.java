package nl.assignment.product.catalog.web;

import nl.assignment.product.catalog.dto.ProductResponse;
import nl.assignment.product.catalog.dto.ProductSearchResponse;
import nl.assignment.product.catalog.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
        /*
         *   AI assisted code
         */
class ProductControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProductService service;


    @Test
    void should_create_product() throws Exception {
        ProductResponse response = new ProductResponse(
                1L, "APL-IPH-18", "Test", "Apple iPhone 18", "Apple", "Smartphones",
                BigDecimal.TEN, 100L, "EUR", null
        );

        Mockito.when(service.create(any())).thenReturn(response);

        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku":"APL-IPH-18",
                                  "name":"Test",
                                  "price":10,
                                  "quantity":100
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("APL-IPH-18"));
    }

    @Test
    void should_search_products() throws Exception {
        ProductSearchResponse doc = new ProductSearchResponse(
                1L, "APL-IPH-18", "Test", null, null, null,
                BigDecimal.TEN, 100L, null
        );

        Mockito.when(service.search(eq("test"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(doc)));

        mvc.perform(get("/api/products/search?q=test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("APL-IPH-18"));
    }

    @Test
    void should_update_quantity() throws Exception {
        Mockito.doNothing().when(service).updateQuantity(eq("APL-IPH-18"), eq(5L));

        mvc.perform(patch("/api/products/APL-IPH-18/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 5
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void should_get_product_by_sku() throws Exception {
        ProductResponse response = new ProductResponse(
                1L, "APL-IPH-18", "Test", "Desc", "Brand", "Cat",
                BigDecimal.TEN, 10L, "EUR", null
        );

        Mockito.when(service.getBySku("APL-IPH-18")).thenReturn(response);

        mvc.perform(get("/api/products/APL-IPH-18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("APL-IPH-18"))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    void should_update_product() throws Exception {
        ProductResponse response = new ProductResponse(
                1L, "APL-IPH-18", "Updated", "NewDesc", "Brand", "Cat",
                BigDecimal.ONE, 5L, "EUR", null
        );

        Mockito.when(service.update(eq(1L), any())).thenReturn(response);

        mvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku":"APL-IPH-18",
                                  "name":"Updated",
                                  "description":"NewDesc",
                                  "price":1,
                                  "quantity":5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }
    
    @Test
    void should_delete_product() throws Exception {
        Mockito.doNothing().when(service).deleteProduct("APL-IPH-18");

        mvc.perform(delete("/api/products/APL-IPH-18"))
                .andExpect(status().isNoContent());
    }

    @Test
    void should_return_not_found_when_deleting_non_existing_product() throws Exception {
        Mockito.doThrow(new EntityNotFoundException("ABC"))
                .when(service).deleteProduct("ABC");

        mvc.perform(delete("/api/products/ABC"))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_list_products_with_pagination_and_sorting() throws Exception {
        ProductResponse response = new ProductResponse(
                1L, "APL-IPH-18", "Test", "Apple iPhone 18", "Apple", "mobile",
                BigDecimal.TEN, 100L, "EUR", null
        );

        Mockito.when(service.list(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mvc.perform(get("/api/products?page=0&size=10&sort=name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("APL-IPH-18"));
    }
}