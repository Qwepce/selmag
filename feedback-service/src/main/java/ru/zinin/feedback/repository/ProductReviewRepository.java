package ru.zinin.feedback.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.zinin.feedback.entity.ProductReview;

import java.util.UUID;

public interface ProductReviewRepository extends ReactiveCrudRepository<ProductReview, UUID> {

    @Query("{'productId': ?0}") // указывается номер аргумента в методе, начинается с нуля
    Flux<ProductReview> findAllByProductId(Integer productId);
}
