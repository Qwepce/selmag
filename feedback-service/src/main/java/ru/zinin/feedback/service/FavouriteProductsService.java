package ru.zinin.feedback.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.zinin.feedback.entity.FavouriteProduct;

public interface FavouriteProductsService {

    Mono<FavouriteProduct> addProductToFavourites(Integer productId, String userId);

    Mono<Void> removeProductFromFavourites(Integer productId, String userId);

    Mono<FavouriteProduct> findFavouriteProductByProduct(Integer productId, String userId);

    Flux<FavouriteProduct> findFavouriteProducts(String userId);
}
