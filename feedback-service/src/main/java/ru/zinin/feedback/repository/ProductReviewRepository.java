package ru.zinin.feedback.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.zinin.feedback.entity.ProductReview;

public interface ProductReviewRepository {

    Mono<ProductReview> saveProductReview(ProductReview productReview);

    Flux<ProductReview> findAllByProductId(Integer productId);
}
