package ru.zinin.feedback.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.zinin.feedback.entity.FavouriteProduct;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@AutoConfigureWebTestClient
@Slf4j
class FavouriteProductsRestControllerIT {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @BeforeEach
    void setUp() {
        this.reactiveMongoTemplate.insertAll(List.of(
                new FavouriteProduct(UUID.fromString("925d2ccd-ed6f-414b-a08d-99eb355ad930"), 1,
                        "92f806b4-9984-4d40-8abe-c6e0c8436a2a"),
                new FavouriteProduct(UUID.fromString("a6893e9e-ec21-4da2-80cd-23695389fa05"), 2,
                        "86768dff-76fb-49a7-a34c-16051bea21b8"),
                new FavouriteProduct(UUID.fromString("12a77188-18a0-4daa-b6a5-e29363a539e4"), 3,
                        "92f806b4-9984-4d40-8abe-c6e0c8436a2a")
        )).blockLast();
    }

    @AfterEach
    void tearDown() {
        this.reactiveMongoTemplate.remove(FavouriteProduct.class).all().block();
    }

    @Test
    void findFavouriteProducts_ReturnsFavouriteProducts() {
        //given

        //when
        this.webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("92f806b4-9984-4d40-8abe-c6e0c8436a2a")))
                .get()
                .uri("/feedback-api/favourite-products")
                .exchange()
        //then
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json("""
                        [
                            {
                                "id": "925d2ccd-ed6f-414b-a08d-99eb355ad930",
                                "productId": 1,
                                "userId": "92f806b4-9984-4d40-8abe-c6e0c8436a2a"
                            },
                            {
                                "id": "12a77188-18a0-4daa-b6a5-e29363a539e4",
                                "productId": 3,
                                "userId": "92f806b4-9984-4d40-8abe-c6e0c8436a2a"
                            }
                        ]""");
    }

    @Test
    void findFavouriteProducts_UserIsNotAuthenticated_ReturnsUnauthorized() {
        //given

        //when
        this.webTestClient
                .get()
                .uri("/feedback-api/favourite-products")
                .exchange()
                //then
                .expectStatus().isUnauthorized();
    }

    @Test
    void findFavouriteProductByProductId_ReturnsFavouriteProduct() {
        //given

        //when
        this.webTestClient
                .mutateWith(mockJwt()
                        .jwt(builder -> builder
                                .claim("sub", "86768dff-76fb-49a7-a34c-16051bea21b8")))
                .get()
                .uri("/feedback-api/favourite-products/by-product-id/2")
                .exchange()
                //then
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json("""
                        {
                            "id": "a6893e9e-ec21-4da2-80cd-23695389fa05",
                            "productId": 2,
                            "userId": "86768dff-76fb-49a7-a34c-16051bea21b8"
                        }""");
    }

    @Test
    void findFavouriteProductByProductId_UserIsUnauthorized_ReturnsUnauthorized() {
        //given

        //when
        this.webTestClient
                .get()
                .uri("/feedback-api/product-reviews/by-product-id/1")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void addProductToFavourites_RequestIsValid_ReturnsFavouriteProduct() {
        //given

        //when
        this.webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.claim("sub", "bdef0765-e1be-4030-8074-f91f45dbea5e")))
                .post()
                .uri("/feedback-api/favourite-products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": 4
                        }
                        """)
                .exchange()
                //then
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json("""
                        {
                            "productId": 4,
                            "userId": "bdef0765-e1be-4030-8074-f91f45dbea5e"
                        }""").jsonPath("$.id").exists();

    }

    @Test
    void addProductToFavourites_RequestIsInvalid_ReturnsBadRequest() {
        //given

        //when
        this.webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.claim("sub", "03d16b8f-f80e-42de-b355-d3b1028d83e7")))
                .post()
                .uri("/feedback-api/favourite-products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": null
                        }""")
                .exchange()
                //then
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody().json("""
                        {
                            "errors": ["Товар не указан"]
                        }""");
    }

    @Test
    void addProductToFavourites_UserIsUnauthorized_ReturnsUnauthorized() {
        //given

        //when
        this.webTestClient
                .post()
                .uri("/feedback-api/favourite-products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": 4
                        }""")
                .exchange()
                //then
                .expectStatus().isUnauthorized();
    }

    @Test
    void removeProductFromFavourites_ReturnsNoContent() {
        //given

        //when
        this.webTestClient.mutateWith(mockJwt().jwt(builder -> builder.claim("sub", "92f806b4-9984-4d40-8abe-c6e0c8436a2a")))
                .delete()
                .uri("/feedback-api/favourite-products/by-product-id/3")
                .exchange()
                //then
                .expectStatus().isNoContent();
    }

    @Test
    void removeProductFromFavourites_UserIsUnauthorized_ReturnsUnauthorized() {
        //given

        //when
        this.webTestClient
                .delete()
                .uri("/feedback-api/favourite-products/by-product-id/3")
                .exchange()
                //then
                .expectStatus().isUnauthorized();
    }
}