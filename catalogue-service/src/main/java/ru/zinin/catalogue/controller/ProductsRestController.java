package ru.zinin.catalogue.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import ru.zinin.catalogue.controller.payload.NewProductPayload;
import ru.zinin.catalogue.entity.Product;
import ru.zinin.catalogue.service.ProductService;

import java.util.Map;

@RestController
@RequestMapping("catalogue-api/products")
@RequiredArgsConstructor
public class ProductsRestController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Iterable<Product>> findAllProducts(@RequestParam(value = "filter", required = false) String filter) {

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.productService.findAllProducts(filter));
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody @Valid NewProductPayload payload,
                                           BindingResult bindingResult,
                                           UriComponentsBuilder uriBuilder) throws BindException {

        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            Product product = this.productService.createProduct(payload.title(), payload.details());
            return ResponseEntity.created(uriBuilder
                            .replacePath("/catalogue-api/products/{productId}")
                            .build(Map.of("productId", product.getId())))
                    .body(product);
        }
    }
}
