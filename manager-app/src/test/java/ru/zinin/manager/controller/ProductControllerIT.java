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
import ru.zinin.manager.controller.payload.UpdateProductPayload;
import ru.zinin.manager.entity.Product;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@WireMockTest(httpPort = 54321)
class ProductControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getProduct_ProductExists_ReturnsProductPage() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1")
                .with(user("j.dewar").roles("MANAGER"));

        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": 1,
                            "title": "Товар",
                            "details": "Описание товара"
                        }""")));
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/product"),
                        model().attribute("product", new Product(1, "Товар", "Описание товара"))
                );
    }

    @Test
    void getProduct_ProductDoesNotExist_ReturnsError404Page() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1")
                .with(user("j.dewar").roles("MANAGER"));
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.notFound()));
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Товар не найден")
                );
    }

    @Test
    void getProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1")
                .with(user("unknown"));
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void getProductEditPage_ProductExists_ReturnsProductEditPage() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1/edit")
                .with(user("j.dewar").roles("MANAGER"));
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                             "id": 1,
                             "title": "Товар",
                             "details": "Описание товара"
                        }""")));
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/edit"),
                        model().attribute("product", new Product(1, "Товар", "Описание товара"))
                );
    }

    @Test
    void getProductEditPage_ProductDoesNotExist_ReturnsError404Page() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1/edit")
                .with(user("j.dewar").roles("MANAGER"));
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.notFound()));
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Товар не найден")
                );
    }

    @Test
    void getProductEditPage_UserIsUnauthorized_ReturnsForbidden() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1/edit")
                .with(user("unknown"));
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void updateProduct_RequestIsValid_RedirectToProductPage() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/edit")
                .param("title", "Новое название")
                .param("details", "Новое описание товара")
                .with(user("j.dewar").roles("MANAGER"))
                .with(csrf());
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": 1,
                            "title" : "Товар",
                            "details": "Описание товара"
                        }""")));
        WireMock.stubFor(WireMock.patch("/catalogue-api/products/1")
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "Новое название",
                            "details": "Новое описание товара"
                        }"""))
                .willReturn(WireMock.noContent()));
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/catalogue/products/1")
                );
    }

    @Test
    void updateProduct_RequestIsInvalid_ReturnsProductEditPage() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/edit")
                .param("title", "  ")
                .with(user("j.dewar").roles("MANAGER"))
                .with(csrf());
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": 1,
                            "title" : "Товар",
                            "details": "Описание товара"
                        }""")));
        WireMock.stubFor(WireMock.patch("/catalogue-api/products/1")
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "  ",
                            "details": null
                        }"""))
                .willReturn(WireMock.badRequest()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody("""
                                {
                                    "errors": ["Ошибка 1", "Ошибка 2"]
                                }""")));
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isBadRequest(),
                        view().name("catalogue/products/edit"),
                        model().attribute("product", new Product(1, "Товар", "Описание товара")),
                        model().attribute("payload", new UpdateProductPayload("  ", null)),
                        model().attribute("errors", List.of("Ошибка 1", "Ошибка 2"))
                );

        WireMock.verify(WireMock.patchRequestedFor(WireMock.urlPathMatching("/catalogue-api/products/1"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "  ",
                            "details": null
                        }""")));
    }

    @Test
    void updateProduct_ProductDoesNotExist_ReturnsProductEditPage() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/edit")
                .param("title", "Новое название")
                .param("details", "Новое описание")
                .with(user("j.dewar").roles("MANAGER"))
                .with(csrf());
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.notFound()));
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Товар не найден")
                );
    }

    @Test
    void updateProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/edit")
                .param("title", "Новое название")
                .param("details", "Новое описание")
                .with(user("unknown"))
                .with(csrf());
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void deleteProduct_ProductExists_RedirectToProductsListPage() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/delete")
                .with(user("j.dewar").roles("MANAGER"))
                .with(csrf());
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": 1,
                            "title": "Товар",
                            "details": "Описание товара"
                        }
                        """)));
        WireMock.stubFor(WireMock.delete("/catalogue-api/products/1")
                .willReturn(WireMock.noContent()));
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/catalogue/products/list")
                );
    }

    @Test
    void deleteProduct_ProductDoesNotExist_Returns404Error() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/delete")
                .with(user("j.dewar").roles("MANAGER"))
                .with(csrf());
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.notFound()));
        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isNotFound(),
                        model().attribute("error", "Товар не найден"),
                        view().name("errors/404")
                );
    }

    @Test
    void deleteProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/delete")
                .with(user("unknown"))
                .with(csrf());

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isForbidden()
                );
    }
}
