package ru.zinin.feedback.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.zinin.feedback.entity.FavouriteProduct;
import ru.zinin.feedback.repository.FavouriteProductRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultFavouriteProductsServiceTest {

    @Mock
    FavouriteProductRepository favouriteProductRepository;

    @InjectMocks
    DefaultFavouriteProductsService service;

    @Test
    void addProductToFavourites_ReturnsCreatedFavouriteProduct() {
        //given
        doAnswer(invocation -> Mono.justOrEmpty(invocation.getArguments()[0]))
                .when(this.favouriteProductRepository).save(any());
        //when
        StepVerifier.create(this.service.addProductToFavourites(1, "a4f8b2ef-046d-4f11-b035-a4fc5fe39735"))
                //then
                .expectNextMatches(favouriteProduct -> favouriteProduct.getProductId() == 1 &&
                        favouriteProduct.getUserId().equals("a4f8b2ef-046d-4f11-b035-a4fc5fe39735") &&
                        favouriteProduct.getId() != null)
                .verifyComplete();

        verify(this.favouriteProductRepository).save(argThat(favouriteProduct -> favouriteProduct.getProductId() == 1 &&
                favouriteProduct.getUserId().equals("a4f8b2ef-046d-4f11-b035-a4fc5fe39735") && favouriteProduct.getId() != null));
    }

    @Test
    void removeProductFromFavourites_ReturnsEmptyMono() {
        //given
        doReturn(Mono.empty()).when(this.favouriteProductRepository)
                .deleteByProductIdAndUserId(1, "a4f8b2ef-046d-4f11-b035-a4fc5fe39735");
        //when
        StepVerifier.create(this.service.removeProductFromFavourites(1, "a4f8b2ef-046d-4f11-b035-a4fc5fe39735"))
                //then
                .verifyComplete();

        verify(this.favouriteProductRepository)
                .deleteByProductIdAndUserId(1, "a4f8b2ef-046d-4f11-b035-a4fc5fe39735");
    }

    @Test
    void findFavouriteProductByProduct_ReturnsFavouriteProduct() {
        //given
        doReturn(Mono.just(new FavouriteProduct(UUID.fromString("fe87eef6-cbd7-11ee-aeb6-275dac91de02"),
                1, "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c")))
                .when(this.favouriteProductRepository).findByProductIdAndUserId(1, "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c");
        //when
        StepVerifier.create(this.service.findFavouriteProductByProduct(1, "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c"))
                .expectNext(new FavouriteProduct(UUID.fromString("fe87eef6-cbd7-11ee-aeb6-275dac91de02"), 1, "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c"))
                .verifyComplete();
        //then
    }

    @Test
    void findFavouriteProducts_ReturnsFavouriteProducts() {
        //given
        doReturn(Flux.fromIterable(List.of(
                new FavouriteProduct(UUID.fromString("133499e3-16e0-4561-a690-8adfdb2fd799"), 1,
                        "0a1251b4-0798-4179-a4e8-33a530072985"),
                new FavouriteProduct(UUID.fromString("d5b4e726-6348-4664-b8ed-4269fa25737d"), 1,
                        "0a1251b4-0798-4179-a4e8-33a530072985")
        ))).when(this.favouriteProductRepository).findAllByUserId("0a1251b4-0798-4179-a4e8-33a530072985");
        //when
        StepVerifier.create(this.service.findFavouriteProducts("0a1251b4-0798-4179-a4e8-33a530072985"))
                //then
                .expectNext(
                        new FavouriteProduct(UUID.fromString("133499e3-16e0-4561-a690-8adfdb2fd799"), 1,
                                "0a1251b4-0798-4179-a4e8-33a530072985"),
                        new FavouriteProduct(UUID.fromString("d5b4e726-6348-4664-b8ed-4269fa25737d"), 1,
                                "0a1251b4-0798-4179-a4e8-33a530072985")
                )
                .verifyComplete();
    }
}