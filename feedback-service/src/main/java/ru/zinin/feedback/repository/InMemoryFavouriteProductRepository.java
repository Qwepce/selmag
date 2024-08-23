package ru.zinin.feedback.repository;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.zinin.feedback.entity.FavouriteProduct;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Repository
public class InMemoryFavouriteProductRepository implements FavouriteProductRepository {

    private final List<FavouriteProduct> favouriteProducts = Collections.synchronizedList(new LinkedList<>());

    @Override
    public Mono<FavouriteProduct> save(FavouriteProduct favouriteProduct) {
        this.favouriteProducts.add(favouriteProduct);
        return Mono.just(favouriteProduct);
    }

    @Override
    public Mono<Void> deleteByProductId(Integer productId) {

        this.favouriteProducts.removeIf(product -> product.getProductId().equals(productId));

        return Mono.empty();
    }

    @Override
    public Mono<FavouriteProduct> findByProductId(Integer productId) {
        return Flux.fromIterable(this.favouriteProducts)
                .filter(favouriteProduct -> favouriteProduct.getProductId().equals(productId))
                .singleOrEmpty();
    }

    @Override
    public Flux<FavouriteProduct> findAll() {
        return Flux.fromIterable(this.favouriteProducts);
    }
}
