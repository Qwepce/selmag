package ru.zinin.catalogue.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.zinin.catalogue.controller.payload.UpdateProductPayload;
import ru.zinin.catalogue.entity.Product;
import ru.zinin.catalogue.service.ProductService;

import java.util.Locale;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("catalogue-api/products/{productId:\\d+}")
@Slf4j
public class ProductRestController {

    private final ProductService productService;
    private final MessageSource messageSource;

    @ModelAttribute("product")
    public Product getProduct(@PathVariable("productId") Integer productId) {
        return this.productService.findProduct(productId).orElseThrow(
                () -> new NoSuchElementException("catalogue.errors.product.not_found")
        );
    }

    @GetMapping
    public ResponseEntity<Product> findProduct(@ModelAttribute("product") Product product) {
        return ResponseEntity.of(productService.findProduct(product.getId()));
    }

    @PatchMapping
    public ResponseEntity<?> updateProduct(@PathVariable("productId") Integer productId,
                                           @Valid @RequestBody UpdateProductPayload payload,
                                           BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            this.productService.updateProduct(productId, payload.title(), payload.details());
            return ResponseEntity.noContent()
                    .build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Integer productId) {
        this.productService.deleteProduct(productId);

        return ResponseEntity.noContent()
                .build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNoSuchElementException(NoSuchElementException exception, Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, this.messageSource.getMessage(exception.getMessage(), new Object[0],
                        exception.getMessage(), locale)
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problemDetail);
    }
}
