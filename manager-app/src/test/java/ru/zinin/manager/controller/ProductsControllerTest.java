package ru.zinin.manager.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ConcurrentModel;
import ru.zinin.manager.client.BadRequestException;
import ru.zinin.manager.client.ProductsRestClient;
import ru.zinin.manager.controller.payload.NewProductPayload;
import ru.zinin.manager.entity.Product;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты ProductsController")
class ProductsControllerTest {

    @Mock
    ProductsRestClient productsRestClient;

    @InjectMocks
    ProductsController controller;

    @Test
    @DisplayName("createProduct создаст новый продукт и перенаправит на его страницу, если запрос валиден")
    void createProduct_RequestIsValid_ReturnsRedirectionToProductPage() {
        //given
        var payload = new NewProductPayload("Новый товар", "Описание нового товара");
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        doReturn(new Product(1, "Новый товар", "Описание нового товара"))
                .when(this.productsRestClient).createProduct("Новый товар", "Описание нового товара");

        //when
        var result = this.controller.createProduct(payload, model, response);

        //then
        assertEquals("redirect:/catalogue/products/1", result);
        verify(this.productsRestClient).createProduct("Новый товар", "Описание нового товара");
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    @DisplayName("createProduct вернёт страницу с ошибками, если запрос не валиден")
    void createProduct_RequestIsInvalid_ReturnsProductFormWithErrors() {
        //given
        var payload = new NewProductPayload("   ", null);
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        doThrow(new BadRequestException(List.of("Ошибка 1", "Ошибка 2")))
                .when(this.productsRestClient)
                .createProduct("   ", null);

        //when
        var result = this.controller.createProduct(payload, model, response);

        //then
        assertEquals("catalogue/products/new_product", result);
        assertEquals(payload, model.getAttribute("payload"));
        assertEquals(List.of("Ошибка 1", "Ошибка 2"), model.getAttribute("errors"));

        verify(this.productsRestClient).createProduct("   ", null);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    @DisplayName("getNewProductPage вернёт страницу создания нового товара")
    void getNewProductPage_ReturnsProductPage() {
        var result = this.controller.getNewProductPage();

        assertEquals("catalogue/products/new_product", result);
    }

    @Test
    @DisplayName("getAllProducts вернёт страницу со списком товаров")
    void getAllProducts_ReturnsProductsListPage() {
        //given
        var model = new ConcurrentModel();
        var filter = "товар";

        var products = IntStream.range(1, 4)
                .mapToObj(i -> new Product(i, "Товар №%d".formatted(i), "Описание товара №%d".formatted(i)))
                .toList();
        doReturn(products).when(this.productsRestClient).findAllProducts(filter);

        //when
        var result = this.controller.getAllProducts(model, filter);

        //then

        assertEquals("catalogue/products/list", result);
        assertEquals(filter, model.getAttribute("filter"));
        assertEquals(products, model.getAttribute("products"));
    }
}