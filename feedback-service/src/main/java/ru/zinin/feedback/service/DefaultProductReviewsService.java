package ru.zinin.feedback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.zinin.feedback.entity.ProductReview;
import ru.zinin.feedback.repository.ProductReviewRepository;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DefaultProductReviewsService implements ProductReviewsService {

    private final ProductReviewRepository productReviewRepository;

    @Override
    public Mono<ProductReview> createProductReview(Integer productId, int rating, String review, String userId) {
        return this.productReviewRepository.save(
                new ProductReview(UUID.randomUUID(), productId, rating, review, userId));
    }

    @Override
    public Flux<ProductReview> findProductReviewsByProduct(Integer productId) {
        return this.productReviewRepository.findAllByProductId(productId);
    }
}
