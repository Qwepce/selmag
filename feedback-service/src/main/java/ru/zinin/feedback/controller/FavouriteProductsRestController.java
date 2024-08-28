package ru.zinin.feedback.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.zinin.feedback.controller.payload.NewFavouriteProductPayload;
import ru.zinin.feedback.entity.FavouriteProduct;
import ru.zinin.feedback.service.FavouriteProductsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("feedback-api/favourite-products")
public class FavouriteProductsRestController {

    private final FavouriteProductsService favouriteProductsService;

    @GetMapping
    public Flux<FavouriteProduct> findFavouriteProducts(Mono<JwtAuthenticationToken> authenticationTokenMono) {
        return authenticationTokenMono.flatMapMany(token ->
                this.favouriteProductsService.findFavouriteProducts(token.getToken().getSubject()));
    }

    @GetMapping("by-product-id/{productId:\\d+}")
    public Mono<FavouriteProduct> findFavouriteProductByProductId(Mono<JwtAuthenticationToken> authenticationTokenMono,
                                                                  @PathVariable("productId") Integer productId) {

        return authenticationTokenMono.flatMap(token ->
                this.favouriteProductsService.findFavouriteProductByProduct(productId, token.getToken().getSubject()));
    }

    @PostMapping
    public Mono<ResponseEntity<FavouriteProduct>> addProductToFavourites(
            Mono<JwtAuthenticationToken> authenticationTokenMono,
            @Valid @RequestBody Mono<NewFavouriteProductPayload> payloadMono,
            UriComponentsBuilder uriComponentsBuilder
    ) {

        return Mono.zip(authenticationTokenMono, payloadMono)
                .flatMap(tuple -> this.favouriteProductsService
                        .addProductToFavourites(tuple.getT2().productId(), tuple.getT1().getToken().getSubject()))
                .map(favouriteProduct -> ResponseEntity
                        .created(uriComponentsBuilder.replacePath("feedback-api/favourite-products/{id}")
                                .build(favouriteProduct.getId()))
                        .body(favouriteProduct));
    }

    @DeleteMapping("by-product-id/{productId:\\d+}")
    public Mono<ResponseEntity<Void>> removeProductFromFavourites(Mono<JwtAuthenticationToken> authenticationTokenMono,
                                                                  @PathVariable("productId") Integer productId) {

        return authenticationTokenMono.flatMap(token -> this.favouriteProductsService
                    .removeProductFromFavourites(productId, token.getToken().getSubject())
                .then(Mono.just(ResponseEntity.noContent().build())));
    }
}
