package ru.zinin.catalogue.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class ProductRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    @Sql("/sql/products.sql")
    void findProduct_ProductExists_ReturnsProduct() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/products/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")));

        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                                {
                                    "id": 1,
                                    "title": "Товар №1",
                                    "details": "Описание товара №1"
                                }""")
                );
    }

    @Test
    void findProduct_ProductDoesNotExist_ReturnsNotFound() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/products/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")));

        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isNotFound()
                );
    }

    @Test
    @Sql("/sql/products.sql")
    void findProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/products/1")
                .with(jwt());

        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    @Sql("/sql/products.sql")
    void updateProduct_RequestIsValid_ReturnNoContent() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Новое название",
                            "details": "Новое описание"
                        }""")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isNoContent()
                );
    }

    @Test
    @Sql("/sql/products.sql")
    void updateProduct_RequestIsInvalid_ReturnsBadRequest() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/products/1")
                .locale(Locale.of("ru"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "  ",
                            "details": null
                        }""")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                                {
                                    "errors": ["Название товара должно быть от 3 до 50 символов"]
                                }""")
                );
    }

    @Test
    void updateProduct_ProductDoesNotExist_ReturnsNotFound() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "  ",
                            "details": null
                        }""")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isNotFound()
                );
    }

    @Test
    @Sql("/sql/products.sql")
    void updateProducts_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Новое название",
                            "details": "Новое описание"
                        }""")
                .with(jwt());
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    @Sql("/sql/products.sql")
    void deleteProduct_ProductExists_ReturnsNoContent() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.delete("/catalogue-api/products/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isNoContent()
                );
    }

    @Test
    void deleteProduct_ProductDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/catalogue-api/products/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNotFound()
                );
    }

    @Test
    @Sql("/sql/products.sql")
    void deleteProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/catalogue-api/products/1")
                .with(jwt());

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isForbidden()
                );
    }
}