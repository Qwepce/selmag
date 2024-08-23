package ru.zinin.feedback.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.zinin.feedback.entity.FavouriteProduct;

public interface FavouriteProductsService {

    Mono<FavouriteProduct> addProductToFavourites(Integer productId);

    Mono<Void> removeProductFromFavourites(Integer productId);

    Mono<FavouriteProduct> findFavouriteProductByProduct(Integer productId);

    Flux<FavouriteProduct> findFavouriteProducts();
}
