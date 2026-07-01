package nl.assignment.product.catalog.web;


import nl.assignment.product.catalog.dto.PriceUpdateRequest;
import nl.assignment.product.catalog.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PriceWebhookController.class)
class PriceWebhookControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void moet_prijsupdate_kunnen_verwerken() throws Exception {
        mvc.perform(post("/api/integrations/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"ABC123\",\"price\":99.99}"))
                .andExpect(status().isAccepted());

        Mockito.verify(productService)
                .updatePrice(eq("ABC123"), eq(BigDecimal.valueOf(99.99)));
    }
}
