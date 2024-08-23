package ru.zinin.customer.client;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.zinin.customer.entity.FavouriteProduct;

public interface FavouriteProductsClient {

    Flux<FavouriteProduct> findFavouriteProducts();

    Mono<FavouriteProduct> findFavouriteProductByProductId(Integer productId);

    Mono<FavouriteProduct> addProductToFavourites(Integer productId);

    Mono<Void> removeProductFromFavourites(Integer productId);

}
