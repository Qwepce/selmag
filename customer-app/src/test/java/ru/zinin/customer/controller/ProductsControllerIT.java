package ru.zinin.customer.controller;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@SpringBootTest
@AutoConfigureWebTestClient
@WireMockTest(httpPort = 54321)
class ProductsControllerIT {

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        stubFor(get(urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", equalTo("фильтр"))
                .willReturn(okJson("""
                        [
                            {
                                "id": 1,
                                "title": "Отфильтрованный товар №1",
                                "details": "Описание отфильтрованного товара №1"
                            },
                            {
                                "id": 2,
                                "title": "Отфильтрованный товар №2",
                                "details": "Описание отфильтрованного товара №2"
                            },
                            {
                                "id": 3,
                                "title": "Отфильтрованный товар №3",
                                "details": "Описание отфильтрованного товара №3"
                            }
                        ]""")));
    }

    @Test
    void getProductsListPage_ReturnsProductsPage() {
        // given

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .get()
                .uri("/customer/products/list?filter=фильтр")
                .exchange()
                // then
                .expectStatus().isOk();

        verify(getRequestedFor(urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", equalTo("фильтр")));
    }

    @Test
    void getProductListPage_UserIsNotAuthenticated_RedirectToLoginPage() {
        //given

        //when
        this.webTestClient
                .get()
                .uri("/customer/products/list?filter=фильтр")
                .exchange()
                //then
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }

    @Test
    void getFavouriteProductsList_ReturnsFavouriteProductsPage() {
        //given
        stubFor(get(urlPathMatching("/feedback-api/favourite-products"))
                .willReturn(okJson("""
                        [
                            {
                                "id": "df293082-5beb-4255-b047-dbacfe51607c",
                                "productId": 1,
                                "userId": "f722c6fe-5312-40e2-8a61-e5cf7c9c4634"
                            },
                            {
                                "id": "c97f84c1-bb36-4f00-b633-999b2f9ad5c7",
                                "productId": 3,
                                "userId": "cefc7859-731e-428d-98d3-798a16c5ac9e"
                            }
                        ]""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
        //when
        this.webTestClient
                .mutateWith(mockUser())
                .get()
                .uri("/customer/products/favourites?filter=фильтр")
                .exchange()
                //then
                .expectStatus().isOk();

        verify(getRequestedFor(urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", equalTo("фильтр")));
        verify(getRequestedFor(urlPathMatching("/feedback-api/favourite-products")));
    }

    @Test
    void getFavouriteProductsList_UserIsNotAuthenticated_RedirectToLoginPage() {
        //given

        //when
        this.webTestClient
                .get()
                .uri("/customer/products/favourites")
                .exchange()
                //then
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }
}