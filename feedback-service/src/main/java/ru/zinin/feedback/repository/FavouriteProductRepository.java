package ru.zinin.feedback.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.zinin.feedback.entity.FavouriteProduct;

public interface FavouriteProductRepository {

    Mono<FavouriteProduct> save(FavouriteProduct favouriteProduct);

    Mono<Void> deleteByProductId(Integer productId);

    Mono<FavouriteProduct> findByProductId(Integer productId);

    Flux<FavouriteProduct> findAll();
}
