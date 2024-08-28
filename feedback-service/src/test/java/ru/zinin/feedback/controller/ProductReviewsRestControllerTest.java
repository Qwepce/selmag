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
import ru.zinin.feedback.controller.payload.NewProductReviewPayload;
import ru.zinin.feedback.entity.ProductReview;
import ru.zinin.feedback.service.ProductReviewsService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductReviewsRestControllerTest {

    @Mock
    ProductReviewsService productReviewsService;

    @InjectMocks
    ProductReviewsRestController controller;

    @Test
    void findProductReviewsByProductId_ReturnsProductReviews() {
        //given
        doReturn(Flux.fromIterable(List.of(
                new ProductReview(UUID.fromString("12c366f6-13d4-4cb1-9225-570526a3c26e"), 1, 1,
                        "Отзыв №1", "User №1"),
                new ProductReview(UUID.fromString("dc3162a9-7fc4-4839-8691-509b8c1e072b"), 1, 2,
                        "Отзыв №2", "User №2"),
                new ProductReview(UUID.fromString("f32034a9-d1f0-4340-be2f-7b4bcb9a01d5"), 1, 3,
                        "Отзыв №3", "User №3")
        ))).when(this.productReviewsService).findProductReviewsByProduct(1);
        //when
        StepVerifier.create(this.controller.findProductReviewsByProductId(1))
                //then
                .expectNext(
                        new ProductReview(UUID.fromString("12c366f6-13d4-4cb1-9225-570526a3c26e"), 1, 1,
                                "Отзыв №1", "User №1"),
                        new ProductReview(UUID.fromString("dc3162a9-7fc4-4839-8691-509b8c1e072b"), 1, 2,
                                "Отзыв №2", "User №2"),
                        new ProductReview(UUID.fromString("f32034a9-d1f0-4340-be2f-7b4bcb9a01d5"), 1, 3,
                                "Отзыв №3", "User №3")
                )
                .verifyComplete();
        verify(this.productReviewsService).findProductReviewsByProduct(1);
        verifyNoMoreInteractions(this.productReviewsService);
    }

    @Test
    void createProductReview_ReturnsCreatedProductReview() {
        //given
        doReturn(Mono.just(new ProductReview(UUID.fromString("0133234c-06c2-48ee-84d3-0dc14300ce73"), 1, 5, "Отзыв №1",
                "fa7f50e4-0e45-4752-a474-6d8ca0c45ab5"))).when(this.productReviewsService)
                .createProductReview(1, 5, "Отзыв №1", "fa7f50e4-0e45-4752-a474-6d8ca0c45ab5");
        //when
        StepVerifier.create(this.controller.createProductReview(
                Mono.just(new JwtAuthenticationToken(Jwt.withTokenValue("e30.e30")
                        .headers(headers -> headers.put("foo", "bar"))
                        .claim("sub", "fa7f50e4-0e45-4752-a474-6d8ca0c45ab5").build())),
                Mono.just(new NewProductReviewPayload(1, 5, "Отзыв №1")),
                UriComponentsBuilder.fromUriString("http://localhost")))
                //then
                .expectNext(ResponseEntity.created(URI
                        .create("http://localhost/feedback-api/product-reviews/0133234c-06c2-48ee-84d3-0dc14300ce73"))
                        .body(new ProductReview(UUID.fromString("0133234c-06c2-48ee-84d3-0dc14300ce73"), 1, 5, "Отзыв №1",
                                "fa7f50e4-0e45-4752-a474-6d8ca0c45ab5")))
                .verifyComplete();

        verify(this.productReviewsService)
                .createProductReview(1, 5, "Отзыв №1", "fa7f50e4-0e45-4752-a474-6d8ca0c45ab5");
        verifyNoMoreInteractions(this.productReviewsService);
    }
}