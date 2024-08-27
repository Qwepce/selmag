package ru.zinin.customer.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.ui.ConcurrentModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.zinin.customer.client.FavouriteProductsClient;
import ru.zinin.customer.client.ProductReviewsClient;
import ru.zinin.customer.client.ProductsClient;
import ru.zinin.customer.controller.payload.NewProductReviewPayload;
import ru.zinin.customer.entity.FavouriteProduct;
import ru.zinin.customer.entity.Product;
import ru.zinin.customer.entity.ProductReview;
import ru.zinin.customer.exception.ClientBadRequestException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    ProductsClient productsClient;

    @Mock
    FavouriteProductsClient favouriteProductsClient;

    @Mock
    ProductReviewsClient productReviewsClient;

    @InjectMocks
    ProductController controller;

    @Test
    void loadProduct_ProductExists_ReturnsNotEmptyMono() {
        //given
        var product = new Product(1, "Товар №1", "Описание товара №1");
        doReturn(Mono.just(product)).when(this.productsClient).findProduct(1);

        //when
        StepVerifier.create(this.controller.loadProduct(1))
                //then
                .expectNext(new Product(1, "Товар №1", "Описание товара №1"))
                .expectComplete().verify();

        verify(this.productsClient).findProduct(1);
        verifyNoInteractions(this.favouriteProductsClient, this.productReviewsClient);
    }

    @Test
    void loadProduct_ProductDoesNotExist_ReturnsMonoWithNoSuchElementException() {
        //given
        doReturn(Mono.empty()).when(this.productsClient).findProduct(1);

        //when
        StepVerifier.create(this.controller.loadProduct(1))
                //then
                .expectErrorMatches(exception ->
                        exception instanceof NoSuchElementException e &&
                        e.getMessage().equals("customer.products.error.not_found"))
                .verify();

        verify(this.productsClient).findProduct(1);
        verifyNoInteractions(this.favouriteProductsClient, this.productReviewsClient);
    }

    @Test
    void getProductPage_ReturnsProductPage() {
        //given
        var model = new ConcurrentModel();
        var productReviews = List.of(
                new ProductReview(UUID.fromString("1348c73e-c157-410a-9329-94e1d6f114b1"), 1, 5, "На пятерочку!"),
                new ProductReview(UUID.fromString("d1e13f70-6d7b-4801-a682-1a4fb18af18a"), 1, 3, "Не очень товар")
        );

        doReturn(Flux.fromIterable(productReviews)).when(this.productReviewsClient).findProductReviewsByProductId(1);

        var favouriteProduct = new FavouriteProduct(UUID.fromString("9d3273fb-f803-4f45-86de-6aac6ac120ad"), 1);
        doReturn(Mono.just(favouriteProduct)).when(this.favouriteProductsClient).findFavouriteProductByProductId(1);

        //when
        StepVerifier.create(this.controller.getProductPage(
                Mono.just(new Product(1, "Товар №1", "Описание товара №1")), model))
                //then
                .expectNext("customer/products/product")
                .verifyComplete();

        assertEquals(productReviews, model.getAttribute("reviews"));
        assertEquals(true, model.getAttribute("inFavourite"));

        verify(this.productReviewsClient).findProductReviewsByProductId(1);
        verify(this.favouriteProductsClient).findFavouriteProductByProductId(1);
        verifyNoMoreInteractions(this.productReviewsClient, this.favouriteProductsClient);
        verifyNoInteractions(this.productsClient);
    }

    @Test
    void addProductToFavourites_RequestIsValid_RedirectToProductPage() {
        //given
        doReturn(Mono.just(new FavouriteProduct(UUID.fromString("f6ae0ecc-66e3-4452-9ee9-4f44a9f074f5"), 1))).when(
                this.favouriteProductsClient).addProductToFavourites(1);
        //when
        StepVerifier.create(this.controller.addProductToFavourites(
                        Mono.just(new Product(1, "Товар №1", "Описание товара №1"))))
                //then
                .expectNext("redirect:/customer/products/1")
                .verifyComplete();

        verify(this.favouriteProductsClient).addProductToFavourites(1);
        verifyNoMoreInteractions(this.favouriteProductsClient);
        verifyNoInteractions(this.productReviewsClient, this.productsClient);
    }

    @Test
    void addProductToFavourites_RequestIsInvalid_RedirectToProductPageWithErrors() {
        //given
        doReturn(Mono.error(new ClientBadRequestException("Возникла какая-то ошибка", null,
                        List.of("Какая-то ошибка"))))
                .when(this.favouriteProductsClient).addProductToFavourites(1);
        //when
        StepVerifier.create(this.controller.addProductToFavourites(
                Mono.just(new Product(1, "Товар №1", "Описание товара №1"))
        ))
                .expectNext("redirect:/customer/products/1")
                .verifyComplete();

        verify(this.favouriteProductsClient).addProductToFavourites(1);
        verifyNoMoreInteractions(this.favouriteProductsClient);
        verifyNoInteractions(this.productsClient, this.productReviewsClient);
    }

    @Test
    void removeProductFromFavourites_RedirectToProductPage() {
        //given
        doReturn(Mono.empty()).when(this.favouriteProductsClient).removeProductFromFavourites(1);
        //when
        StepVerifier.create(this.controller.removeProductFromFavourites(
                Mono.just(new Product(1, "Товар №1", "Описание товара №1"))))
                //then
                .expectNext("redirect:/customer/products/1")
                .verifyComplete();

        verify(this.favouriteProductsClient).removeProductFromFavourites(1);
        verifyNoMoreInteractions(this.favouriteProductsClient);
        verifyNoInteractions(this.productReviewsClient, this.productsClient);
    }

    @Test
    void createReview_RequestIsValid_RedirectToProductPage() {
        //given
        var model = new ConcurrentModel();
        var response = new MockServerHttpResponse();
        doReturn(Mono.just(new ProductReview(UUID.fromString("86efa22c-cbae-11ee-ab01-679baf165fb7"), 1,
                3, "Ну, на троечку"))).when(this.productReviewsClient).createProductReview(
                        1, 3, "Ну, на троечку"
        );
        //when
        StepVerifier.create(this.controller.createReview(1,
                        new NewProductReviewPayload(3, "Ну, на троечку"),
                        model,
                        response))
                //then
                .expectNext("redirect:/customer/products/1")
                .verifyComplete();

        verify(this.productReviewsClient).createProductReview(1, 3, "Ну, на троечку");
        verifyNoMoreInteractions(this.productReviewsClient);
        verifyNoInteractions(this.productsClient, this.favouriteProductsClient);
    }

    @Test
    void createReview_RequestIsInvalid_RedirectToProductPageWithPayloadAndErrors() {
        //given
        var model = new ConcurrentModel();
        var response = new MockServerHttpResponse();

        var favouriteProduct = new FavouriteProduct(UUID.fromString("af5f9496-cbaa-11ee-a407-27b46917819e"), 1);
        doReturn(Mono.just(favouriteProduct)).when(this.favouriteProductsClient).findFavouriteProductByProductId(1);

        doReturn(Mono.error(new ClientBadRequestException("Возникла какая-то ошибка", null, List.of("Какая-то ошибка 1", "Какая-то ошибка 2"))))
                .when(this.productReviewsClient).createProductReview(1, null, "Очень длинный отзыв");
        //when
        StepVerifier.create(this.controller.createReview(1,
                new NewProductReviewPayload( null, "Очень длинный отзыв"),
                model,
                response))
                .expectNext("customer/products/product")
                .verifyComplete();

        verifyNoInteractions(this.productsClient);
    }

    @Test
    void handleNoSuchElementException_ReturnsErrors404Page() {
        //given
        var exception = new NoSuchElementException("Товар не найден");
        var model = new ConcurrentModel();
        var response = new MockServerHttpResponse();

        //when
        var result = this.controller.handleNoSuchElementException(exception, model, response);

        //then
        assertEquals("errors/404", result);
        assertEquals("Товар не найден", model.getAttribute("error"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verifyNoInteractions(this.favouriteProductsClient);
        verifyNoInteractions(this.productReviewsClient);
        verifyNoInteractions(this.productsClient);
    }
}