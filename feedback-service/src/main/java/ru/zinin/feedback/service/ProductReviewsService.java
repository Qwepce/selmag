package ru.zinin.feedback.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.zinin.feedback.entity.ProductReview;

public interface ProductReviewsService {

    Mono<ProductReview> createProductReview(Integer productId, int rating, String review, String userId);

    Flux<ProductReview> findProductReviewsByProduct(Integer productId);
}
