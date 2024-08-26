package ru.zinin.customer.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.zinin.customer.client.payload.NewFavouriteProductPayload;
import ru.zinin.customer.entity.FavouriteProduct;
import ru.zinin.customer.exception.ClientBadRequestException;

import java.util.List;

@RequiredArgsConstructor
public class WebClientFavouriteProductsClient implements FavouriteProductsClient {

    private final WebClient webClient;

    @Override
    public Flux<FavouriteProduct> findFavouriteProducts() {
        return this.webClient.get()
                .uri("/feedback-api/favourite-products")
                .retrieve()
                .bodyToFlux(FavouriteProduct.class);
    }

    @Override
    public Mono<FavouriteProduct> findFavouriteProductByProductId(Integer productId) {
        return this.webClient.get()
                .uri("/feedback-api/favourite-products/by-product-id/{productId}", productId)
                .retrieve()
                .bodyToMono(FavouriteProduct.class)
                .onErrorComplete(WebClientResponseException.NotFound.class);
    }

    @Override
    public Mono<FavouriteProduct> addProductToFavourites(Integer productId) {
        return this.webClient.post()
                .uri("/feedback-api/favourite-products", productId)
                .bodyValue(new NewFavouriteProductPayload(productId))
                .retrieve()
                .bodyToMono(FavouriteProduct.class)
                .onErrorMap(WebClientResponseException.BadRequest.class,
                        exception -> new ClientBadRequestException("Возникла какая-то ошибка при добавлении товара в избранное",
                                exception, ((List<String>) exception
                                .getResponseBodyAs(ProblemDetail.class)
                                .getProperties().get("errors"))));
    }

    @Override
    public Mono<Void> removeProductFromFavourites(Integer productId) {
        return this.webClient.delete()
                .uri("/feedback-api/favourite-products/by-product-id/{productId}", productId)
                .retrieve()
                .toBodilessEntity()
                .then();
    }
}
