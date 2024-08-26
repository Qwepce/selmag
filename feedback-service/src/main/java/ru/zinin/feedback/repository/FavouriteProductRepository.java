package ru.zinin.feedback.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.zinin.feedback.entity.FavouriteProduct;

import java.util.UUID;

public interface FavouriteProductRepository extends
        ReactiveCrudRepository<FavouriteProduct, UUID> {

    Flux<FavouriteProduct> findAllByUserId(String userId);

    Mono<Void> deleteByProductIdAndUserId(Integer productId, String userId);

    Mono<FavouriteProduct> findByProductIdAndUserId(Integer productId, String userId);
}
