package ru.zinin.manager.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.zinin.manager.controller.payload.NewProductPayload;
import ru.zinin.manager.entity.Product;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@WireMockTest(httpPort = 54321)
class ProductsControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getAllProducts_ReturnsProductsListPage() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/list")
                .queryParam("filter", "товар")
                .with(user("j.dewar").roles("MANAGER"));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/catalogue-api/products"))
                        .withQueryParam("filter", WireMock.equalTo("товар"))
                        .willReturn(WireMock.ok("""
                                [
                                    {"id": 1, "title": "Товар №1", "details": "Описание товара №1"},
                                    {"id": 2, "title": "Товар №2", "details": "Описание товара №2"}
                                ]
                                """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
                //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/list"),
                        model().attribute("filter", "товар"),
                        model().attribute("products", List.of(
                                new Product(1, "Товар №1", "Описание товара №1"),
                                new Product(2, "Товар №2", "Описание товара №2")
                        ))
                );
        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", WireMock.equalTo("товар")));
    }

    @Test
    void getAllProducts_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/products")
                .with(user("unknown"));

        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void getNewProductPage_ReturnsProductPage() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/create")
                .with(user("j.dewar").roles("MANAGER"));

        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/new_product")
                );
    }

    @Test
    void getNewProductPage_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/create")
                .with(user("unknown"));
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void createProduct_RequestIsValid_RedirectToProductPage() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/create")
                .param("title", "Новый товар")
                .param("details", "Описание нового товара")
                .with(user("j.dewar").roles("MANAGER"))
                .with(csrf());

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/catalogue-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "Новый товар",
                            "details": "Описание нового товара"
                        }"""))
                .willReturn(WireMock.created()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "id": 1,
                                    "title": "Новый товар",
                                    "details": "Описание нового товара"
                                }""")));
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().is3xxRedirection(),
                        header().string(HttpHeaders.LOCATION, "/catalogue/products/1")
                );
        WireMock.verify(WireMock.postRequestedFor(WireMock.urlPathMatching("/catalogue-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "Новый товар",
                            "details": "Описание нового товара"
                        }""")));
    }

    @Test
    void createProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/create")
                .param("title", "Новый товар")
                .param("details", "Описание товара")
                .with(user("unknown"))
                .with(csrf());

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isForbidden()
                );
    }
}