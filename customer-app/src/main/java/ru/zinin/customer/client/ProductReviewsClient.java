package ru.zinin.customer.client;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.zinin.customer.entity.ProductReview;

public interface ProductReviewsClient {

    Flux<ProductReview> findProductReviewsByProductId(Integer productId);

    Mono<ProductReview> createProductReview(Integer productId, Integer rating, String review);
}
