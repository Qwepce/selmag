package ru.zinin.feedback.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.zinin.feedback.entity.ProductReview;
import ru.zinin.feedback.repository.ProductReviewRepository;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultProductReviewsServiceTest {

    @Mock
    ProductReviewRepository productReviewRepository;

    @InjectMocks
    DefaultProductReviewsService service;

    @Test
    void createProductReview_ReturnsCreatedProductReview() {
        //given
        doAnswer(invocation -> Mono.justOrEmpty(invocation.getArguments()[0]))
                .when(this.productReviewRepository).save(any());
        //when
        StepVerifier.create(this.service.createProductReview(
                1, 3, "На троечку", "a29274a5-5afe-45a9-9281-4e0302d9b085"
        ))
                //then
                .expectNextMatches(
                        productReview -> productReview.getProductId() == 1 &&
                                         productReview.getId() != null &&
                                         productReview.getReview().equals("На троечку") &&
                                         productReview.getUserId().equals("a29274a5-5afe-45a9-9281-4e0302d9b085") &&
                                         productReview.getRating() == 3
                )
                .verifyComplete();
        verify(this.productReviewRepository)
                .save(argThat(productReview ->
                        productReview.getProductId() == 1 && productReview.getRating() == 3 &&
                        productReview.getId() != null &&
                        productReview.getUserId().equals("a29274a5-5afe-45a9-9281-4e0302d9b085") &&
                        productReview.getReview().equals("На троечку")));
    }

    @Test
    void findProductReviewsByProduct_ReturnsProductReviews() {
        //given
        doReturn(Flux.fromIterable(List.of(
                new ProductReview(UUID.fromString("c479c6bc-e124-4c14-b903-d1946ed5e6d2"), 1,
                        3, "Отзыв №1", "user-1"),
                new ProductReview(UUID.fromString("4c5d9b00-ff10-416f-985c-31cfbad05804"), 1,
                        5, "Отзыв №2", "user-2")
        ))).when(this.productReviewRepository).findAllByProductId(1);
        //when
        StepVerifier.create(this.service.findProductReviewsByProduct(1))
                //then
                .expectNext(
                        new ProductReview(UUID.fromString("c479c6bc-e124-4c14-b903-d1946ed5e6d2"), 1,
                                3, "Отзыв №1", "user-1"),
                        new ProductReview(UUID.fromString("4c5d9b00-ff10-416f-985c-31cfbad05804"), 1,
                                5, "Отзыв №2", "user-2")
                )
                .verifyComplete();
    }
}