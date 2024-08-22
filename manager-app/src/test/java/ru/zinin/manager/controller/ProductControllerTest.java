package ru.zinin.manager.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ConcurrentModel;
import ru.zinin.manager.client.BadRequestException;
import ru.zinin.manager.client.ProductsRestClient;
import ru.zinin.manager.controller.payload.UpdateProductPayload;
import ru.zinin.manager.entity.Product;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты ProductController")
class ProductControllerTest {

    @Mock
    ProductsRestClient productsRestClient;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    ProductController controller;

    @Test
    void product_ProductExists_ReturnsProduct() {
        //given
        var product = new Product(1, "Новый товар", "Описание нового товара");
        doReturn(Optional.of(product)).when(this.productsRestClient).findProduct(1);
        //when
        var result = this.controller.product(1);
        //then
        assertEquals(product, result);

        verify(this.productsRestClient).findProduct(1);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void product_ProductDoesNotExist_ReturnsNoSuchElementException() {
        //given

        //when
        var exception = assertThrows(NoSuchElementException.class, () -> this.controller.product(1));
        //then
        assertEquals("catalogue.errors.product.not_found", exception.getMessage());
        verify(this.productsRestClient).findProduct(1);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void getProductById_ReturnsProductPage() {
        //given

        //when
        var result = this.controller.getProduct();

        //then
        assertEquals("catalogue/products/product", result);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void getProductEditPage_ReturnsProductEditPage() {
        //given

        //when
        var result = this.controller.getProductEditPage();

        //then
        assertEquals("catalogue/products/edit", result);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void updateProduct_RequestIsValid_RedirectToProductPage() {
        //given
        var product = new Product(1, "Товар №1", "Описание товара №1");
        var payload = new UpdateProductPayload("Новое название", "Новое описание");
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();
        //when
        var result = this.controller.updateProduct(product, payload, model, response);
        //then
        assertEquals("redirect:/catalogue/products/1", result);
        verify(this.productsRestClient).updateProduct(1, "Новое название", "Новое описание");
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void updateProduct_RequestIsInvalid_ReturnsEditPageWithErrors() {
        //given
        var product = new Product(1, "Товар №1", "Описание товара №1");
        var payload = new UpdateProductPayload("   ", null);
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        doThrow(new BadRequestException(List.of("Ошибка 1", "Ошибка 2")))
                .when(this.productsRestClient).updateProduct(1, "   ", null);
        //when
        var result = this.controller.updateProduct(product, payload, model, response);
        //then
        assertEquals("catalogue/products/edit", result);
        assertEquals(payload, model.getAttribute("payload"));
        assertEquals(List.of("Ошибка 1", "Ошибка 2"), model.getAttribute("errors"));

        verify(this.productsRestClient).updateProduct(1, "   ", null);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void deleteProduct_RedirectToProductsPage() {
        //given
        var product = new Product(1, "Новый товар", "Описание нового товара");

        //when
        var result = this.controller.deleteProduct(product);

        //then
        assertEquals("redirect:/catalogue/products/list", result);
        verify(this.productsRestClient).deleteProduct(1);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void handleNoSuchElementException_Returns404ErrorPage() {
        //given
        var exception = new NoSuchElementException("error");
        var model = new ConcurrentModel();
        var locale = new Locale("ru", "RU");
        var response = new MockHttpServletResponse();
        //when
        var result = this.controller.handleNoSuchElementException(exception, model, response, locale);
        //then
        assertEquals("errors/404", result);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

        verify(this.messageSource).getMessage("error", new Object[0], "error",
                new Locale("ru", "RU"));
        verifyNoMoreInteractions(this.messageSource);
        verifyNoInteractions(this.productsRestClient);
    }
}