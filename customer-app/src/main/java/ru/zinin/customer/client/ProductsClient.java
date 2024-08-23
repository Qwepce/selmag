package ru.zinin.customer.client;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.zinin.customer.entity.Product;

public interface ProductsClient {

    Flux<Product> findAllProducts(String filter);

    Mono<Product> findProduct(Integer productId);
}
