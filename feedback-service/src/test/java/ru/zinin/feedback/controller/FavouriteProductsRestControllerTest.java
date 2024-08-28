package ru.zinin.feedback.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.zinin.feedback.controller.payload.NewFavouriteProductPayload;
import ru.zinin.feedback.entity.FavouriteProduct;
import ru.zinin.feedback.service.FavouriteProductsService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavouriteProductsRestControllerTest {

    @Mock
    FavouriteProductsService favouriteProductsService;

    @InjectMocks
    FavouriteProductsRestController controller;

    @Test
    void findFavouriteProducts_ReturnsFavouriteProduct() {
        //given
        doReturn(Flux.fromIterable(List.of(
                new FavouriteProduct(UUID.fromString("07bf1364-9411-4fff-98a8-8603eb09bd8b"), 1,
                        "26db4961-21f2-43bc-9bcc-62e13762e0f4"),
                new FavouriteProduct(UUID.fromString("edc50c96-d975-4546-bddd-60e1fda14756"), 1,
                        "26db4961-21f2-43bc-9bcc-62e13762e0f4")
        ))).when(this.favouriteProductsService)
                .findFavouriteProducts("26db4961-21f2-43bc-9bcc-62e13762e0f4");
        //when
        StepVerifier.create(this.controller.findFavouriteProducts(
                        Mono.just(new JwtAuthenticationToken(Jwt.withTokenValue("e30.e30")
                                .headers(headers -> headers.put("foo", "bar"))
                                .claim("sub", "26db4961-21f2-43bc-9bcc-62e13762e0f4").build()))))
                //then
                .expectNext(
                        new FavouriteProduct(UUID.fromString("07bf1364-9411-4fff-98a8-8603eb09bd8b"), 1,
                                "26db4961-21f2-43bc-9bcc-62e13762e0f4"),
                        new FavouriteProduct(UUID.fromString("edc50c96-d975-4546-bddd-60e1fda14756"), 1,
                                "26db4961-21f2-43bc-9bcc-62e13762e0f4")
                ).verifyComplete();

        verify(this.favouriteProductsService)
                .findFavouriteProducts("26db4961-21f2-43bc-9bcc-62e13762e0f4");
        verifyNoMoreInteractions(this.favouriteProductsService);
    }

    @Test
    void findFavouriteProductByProductId_ReturnsFavouriteProduct() {
        //given
        doReturn(Mono.just(
                new FavouriteProduct(UUID.fromString("e5efbed0-1e28-4d5b-9c0f-6683e8f8646d"), 1,
                        "26db4961-21f2-43bc-9bcc-62e13762e0f4")
        )).when(this.favouriteProductsService)
                .findFavouriteProductByProduct(1, "26db4961-21f2-43bc-9bcc-62e13762e0f4");
        //when
        StepVerifier.create(this.controller.findFavouriteProductByProductId(
                        Mono.just(new JwtAuthenticationToken(Jwt.withTokenValue("e30.e30")
                                .headers(headers -> headers.put("foo", "bar"))
                                .claim("sub", "26db4961-21f2-43bc-9bcc-62e13762e0f4").build())),
                        1))
                //then
                .expectNext(
                        new FavouriteProduct(UUID.fromString("e5efbed0-1e28-4d5b-9c0f-6683e8f8646d"), 1,
                                "26db4961-21f2-43bc-9bcc-62e13762e0f4")
                ).verifyComplete();

        verify(this.favouriteProductsService)
                .findFavouriteProductByProduct(1, "26db4961-21f2-43bc-9bcc-62e13762e0f4");
        verifyNoMoreInteractions(this.favouriteProductsService);
    }

    @Test
    void addProductToFavourites_ReturnsProductPage() {
        //given
        var token = new JwtAuthenticationToken(Jwt.withTokenValue("e30.e30")
                .headers(headers -> headers.put("foo", "bar"))
                .claim("sub", "88daccdf-7319-4f8c-b5c5-ccb22f9620ff").build());
        doReturn(Mono.just(
                new FavouriteProduct(UUID.fromString("01a10dc4-702b-4e8e-b41c-29072e721f0e"),
                        1, "88daccdf-7319-4f8c-b5c5-ccb22f9620ff")
        )).when(this.favouriteProductsService)
                .addProductToFavourites(1, "88daccdf-7319-4f8c-b5c5-ccb22f9620ff");
        //when
        StepVerifier.create(this.controller.addProductToFavourites(
                        Mono.just(token),
                        Mono.just(new NewFavouriteProductPayload(1)),
                        UriComponentsBuilder.fromUriString("http://localhost")
                ))
                //then
                .expectNext(ResponseEntity
                        .created(URI.create("http://localhost/feedback-api/favourite-products/01a10dc4-702b-4e8e-b41c-29072e721f0e"))
                        .body(new FavouriteProduct(UUID.fromString("01a10dc4-702b-4e8e-b41c-29072e721f0e"), 1, "88daccdf-7319-4f8c-b5c5-ccb22f9620ff")))
                .verifyComplete();

        verify(this.favouriteProductsService).addProductToFavourites(1, "88daccdf-7319-4f8c-b5c5-ccb22f9620ff");
        verifyNoMoreInteractions(this.favouriteProductsService);
    }

    @Test
    void removeProductFromFavourites_ReturnsProductPage() {
        //given
        doReturn(Mono.empty())
                .when(this.favouriteProductsService).removeProductFromFavourites(1, "a63b4732-3396-4703-ac39-e4f780c05cc7");
        //when
        StepVerifier.create(this.controller.removeProductFromFavourites(
                Mono.just(new JwtAuthenticationToken(Jwt.withTokenValue("e30.e30")
                        .headers(headers -> headers.put("foo", "bar"))
                        .claim("sub", "a63b4732-3396-4703-ac39-e4f780c05cc7").build())),
                1
        ))
        //then
                .expectNext(ResponseEntity.noContent().build())
                .verifyComplete();

        verify(this.favouriteProductsService).removeProductFromFavourites(1, "a63b4732-3396-4703-ac39-e4f780c05cc7");
        verifyNoMoreInteractions(this.favouriteProductsService);
    }
}