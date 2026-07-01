package nl.assignment.product.catalog.web;

import nl.assignment.product.catalog.dto.PriceUpdateRequest;
import nl.assignment.product.catalog.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integrations/prices")
public class PriceWebhookController {

    private final ProductService productService;

    public PriceWebhookController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updatePrice(@RequestBody PriceUpdateRequest request) {
        productService.updatePrice(request.getSku(), request.getPrice());
    }
}
