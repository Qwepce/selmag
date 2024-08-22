package ru.zinin.catalogue.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import ru.zinin.catalogue.controller.payload.UpdateProductPayload;
import ru.zinin.catalogue.entity.Product;
import ru.zinin.catalogue.service.ProductService;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRestControllerTest {

    @Mock
    ProductService productService;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    ProductRestController productRestController;

    @Test
    void getProduct_ProductExists_ReturnsProduct() {
        //given
        var product = new Product(1, "Товар", "Описание товара");
        doReturn(Optional.of(product)).when(this.productService).findProduct(1);

        //when
        var result = this.productRestController.getProduct(1);

        //then
        assertNotNull(result);
        assertEquals(product, result);
        verify(this.productService).findProduct(1);
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void getProduct_ProductDoesNotExist_ThrowsNoSuchElementException() {
        //given

        //when
        var exception = assertThrows(NoSuchElementException.class, () -> this.productRestController.getProduct(1));

        //then
        assertEquals("catalogue.errors.product.not_found", exception.getMessage());
    }

    @Test
    void findProduct_ReturnsProduct() {
        //given
        var product = new Product(1, "Товар", "Описание товара");

        //when
        var result = this.productRestController.findProduct(product);

        //then
        assertEquals(product, result);
    }

    @Test
    void updateProduct_RequestIsValid_ReturnsNoContent() throws BindException {
        //given
        var payload = new UpdateProductPayload("Новое описание", "Новое название");
        var bindingResult = new MapBindingResult(Map.of(), "payload");

        //when
        var result = this.productRestController.updateProduct(1, payload, bindingResult);

        //then
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(this.productService).updateProduct(1, "Новое описание", "Новое название");
    }

    @Test
    void updateProduct_RequestIsInvalid_ReturnsBadRequest() {
        //given
        var payload = new UpdateProductPayload("   ", null);
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "title", "error"));

        //when
        var exception = assertThrows(BindException.class,
                () -> this.productRestController.updateProduct(1, payload, bindingResult));

        //then
        assertEquals(List.of(new FieldError("payload", "title", "error")),
                exception.getAllErrors());
        verifyNoInteractions(this.productService);
    }

    @Test
    void updateProduct_RequestIsInvalidAndBindResultIsBindException_ReturnsBadRequest() {
        //given
        var payload = new UpdateProductPayload("   ", null);
        var bindingResult = new BindException(new MapBindingResult(Map.of(), "payload"));
        bindingResult.addError(new FieldError("payload", "title", "error"));
        //when
        var exception = assertThrows(BindException.class,
                () -> this.productRestController.updateProduct(1, payload, bindingResult));
        //then
        assertEquals(List.of(new FieldError("payload", "title", "error")),
                exception.getAllErrors());
        verifyNoInteractions(this.productService);
    }

    @Test
    void deleteProduct_ReturnsNoContent() {
        //given

        //when
        var result = this.productRestController.deleteProduct(1);

        //then
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(this.productService).deleteProduct(1);
    }

    @Test
    void handleNoSuchElementException_ReturnsNotFound() {
        //given
        var exception = new NoSuchElementException("error_code");
        var locale = new Locale("ru", "RU");

        doReturn("error details").when(this.messageSource)
                .getMessage("error_code", new Object[0], "error_code", locale);

        //when
        var result = this.productRestController.handleNoSuchElementException(exception, locale);

        //then
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertInstanceOf(ProblemDetail.class, result.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getBody().getStatus());
        assertEquals("error details", result.getBody().getDetail());

        verifyNoInteractions(this.productService);
    }
}
