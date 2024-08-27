package ru.zinin.feedback.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;
import ru.zinin.feedback.entity.ProductReview;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureWebTestClient
@Slf4j
class ProductReviewsRestControllerIT {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @BeforeEach
    void setUp() {
        this.reactiveMongoTemplate.insertAll(List.of(
                new ProductReview(UUID.fromString("f089dc77-3418-4722-85ad-fbd7196042ae"), 1,
                        1, "Отзыв №1", "user-1"),
                new ProductReview(UUID.fromString("7aa55ae0-aa70-49d0-93d9-52574342b937"), 1,
                        3, "Отзыв №2", "user-2"),
                new ProductReview(UUID.fromString("ee049d82-9900-4c2f-be5f-ae9d6a8c6886"), 1,
                        5, "Отзыв №3", "user-3")
        )).blockLast();
    }

    @AfterEach
    void tearDown() {
        this.reactiveMongoTemplate.remove(ProductReview.class).all().block();
    }

    @Test
    void findProductReviewsByProductId_ReturnsReviews() throws Exception {
        //given

        //when

        //then
        this.webTestClient.mutateWith(mockJwt())
                .mutate().filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
                    log.info("========== REQUEST ==========");
                    log.info("{}, {}", clientRequest.method(), clientRequest.url());
                    clientRequest.headers().forEach((header, value) -> log.info("{}: {}", header, value));
                    log.info("========= END REQUEST ==========");
                    return Mono.just(clientRequest);
                }))
                .build()
                .get()
                .uri("/feedback-api/product-reviews/by-product-id/1")
                .exchange()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .json("""
                        [
                            {
                                "id": "f089dc77-3418-4722-85ad-fbd7196042ae",
                                "productId": 1,
                                "rating": 1,
                                "review": "Отзыв №1",
                                "userId": "user-1"
                            },
                            {
                                "id": "7aa55ae0-aa70-49d0-93d9-52574342b937",
                                "productId": 1,
                                "rating": 3,
                                "review": "Отзыв №2",
                                "userId": "user-2"
                            },
                            {
                                "id": "ee049d82-9900-4c2f-be5f-ae9d6a8c6886",
                                "productId": 1,
                                "rating": 5,
                                "review": "Отзыв №3",
                                "userId": "user-3"
                            }
                        ]""");
    }

    @Test
    void createProductReview_RequestIsValid_ReturnsCreatedProductReview() throws Exception {
        //given

        //when
        this.webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("user-tester")))
                .post()
                .uri("/feedback-api/product-reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": 1,
                            "rating": 5,
                            "review": "На пятерочку!"
                        }""")
                .exchange()
        //then
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .json("""
                        {
                            "productId": 1,
                            "rating": 5,
                            "review": "На пятерочку!",
                            "userId": "user-tester"
                        }""").jsonPath("$.id").exists();
    }

    @Test
    void createProductReview_RequestIsInvalid_ReturnsBadRequest () throws Exception {
        //given

        //when
        this.webTestClient.mutateWith(mockJwt().jwt(builder -> builder.subject("user-tester")))
                .post()
                .uri("/feedback-api/product-reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": null,
                            "rating": -1,
                            "review": "Lorem ipsum odor amet, consectetuer adipiscing elit. Sociosqu facilisis mollis eros at, hac cursus tincidunt. Maecenas parturient conubia aliquam cubilia aliquam sed dolor. Vulputate consequat posuere cras maecenas curae dignissim. Arcu neque facilisis fusce pulvinar sed. Fames aliquet diam elementum finibus magnis quisque, dapibus dictum. Dolor arcu amet torquent sapien fusce nulla elit nec. Nascetur sollicitudin tristique habitant habitasse curabitur tincidunt eros hac nullam. Cursus magnis sed varius; donec a euismod. Venenatis justo curae in a suspendisse tristique. Ad imperdiet quisque erat vehicula gravida at nisl etiam. Nisi vulputate torquent elementum laoreet vel augue rhoncus. Ex netus placerat cursus magna condimentum litora parturient. Ornare magnis malesuada iaculis rutrum etiam velit sodales sed. Nisl velit erat rhoncus porttitor sollicitudin augue. Ex litora neque non facilisis curae morbi augue integer consequat? Leo dis cras ex duis scelerisque dis. Nisi commodo habitasse aliquam tristique platea potenti. Class taciti tincidunt ligula velit tristique. Fusce varius velit amet pharetra cras sollicitudin. Commodo tincidunt lacinia fringilla, ut hendrerit litora. Pellentesque class non volutpat; pellentesque felis nascetur curae. Tempus donec malesuada sodales, dictumst praesent proin. Class vehicula facilisi pharetra urna tristique feugiat natoque. Non habitant ligula maximus ex rutrum vulputate. Molestie suscipit nunc in, tempor posuere pretium consequat. Sociosqu eleifend varius vulputate adipiscing sollicitudin pharetra congue. Lectus proin facilisis eleifend arcu arcu, nisi dictum. Suspendisse eu hendrerit class quam mollis convallis vel posuere. Platea cras non vulputate sociosqu donec enim pulvinar dolor. Placerat felis dignissim vulputate lacinia gravida dis. Vehicula porttitor nisi potenti cubilia taciti habitasse. Mattis tortor convallis fusce varius efficitur tempus hac. Ipsum hac euismod sagittis praesent nunc lobortis? Sed curae aliquet elementum in semper ex. Justo senectus elementum curabitur vehicula id. Nunc congue ullamcorper eleifend diam metus augue dolor eros. Risus penatibus arcu aenean tristique cubilia commodo laoreet sollicitudin. Quis dignissim nullam finibus dis egestas primis justo rutrum augue. Nibh himenaeos eu aliquam dui torquent pharetra ad egestas morbi. Sapien parturient ultrices feugiat mollis, nulla aliquam duis. Integer quisque quisque convallis aliquet hendrerit faucibus semper rhoncus sit. Dictum dolor dignissim odio sed efficitur consectetur ultrices tortor. Class accumsan congue vivamus semper conubia fringilla odio dictum tellus. Nisl dui malesuada purus nec aliquet primis accumsan lacus. Ligula nibh placerat neque montes phasellus. Massa lobortis ultrices eleifend scelerisque laoreet eros tincidunt. Nibh euismod nullam erat tempus pharetra dui sit neque. Dignissim odio fringilla vehicula finibus class eleifend ultricies. Ridiculus odio aliquam augue scelerisque massa senectus. Habitasse neque nunc rutrum mi consequat himenaeos; nunc etiam. Et dapibus proin dapibus nostra euismod mollis. Id cursus tortor quisque porta luctus vivamus. Faucibus pretium nulla inceptos libero turpis. Consectetur himenaeos leo sollicitudin class conubia dis. Felis nulla dictum sollicitudin dignissim hac natoque lorem duis vehicula. Nisl cursus amet bibendum orci morbi conubia iaculis proin. Orci pulvinar conubia torquent per aenean. Facilisi ridiculus nascetur arcu ante porttitor eget lorem quisque facilisi. Dapibus quisque praesent amet ligula nisi sem sodales tempus dictum. Vulputate dis ridiculus nam elementum fusce convallis parturient sed efficitur. Vel ac rhoncus praesent habitant cubilia dolor ut suspendisse? Egestas sem nostra molestie vivamus auctor, augue suspendisse congue urna. Himenaeos montes sem leo nulla morbi; litora dis leo. Vitae viverra molestie placerat erat maecenas eu viverra elementum dis. Curae fermentum consectetur dictumst penatibus varius scelerisque tempus blandit litora. Amet in sociosqu ad fusce volutpat lobortis cubilia suspendisse. Ullamcorper vestibulum condimentum cubilia dictumst tristique egestas. Nunc gravida nulla placerat fringilla sem nam ac. Nulla class purus orci sit quam imperdiet euismod feugiat. Lobortis orci finibus porttitor; aliquam euismod phasellus. Donec nibh nullam himenaeos accumsan; ad sapien maecenas. Vitae ultrices tortor aliquam facilisis lobortis lorem cursus? Dolor fames auctor semper maximus nullam semper aliquam vitae. Vehicula curae non litora suscipit sit lobortis. Porta hac nisi natoque fermentum dignissim. Facilisi nibh consequat venenatis egestas curabitur faucibus conubia habitant. Pharetra ligula arcu aptent quam in augue? Enim massa augue class tortor venenatis amet magna velit. Neque aliquet dis vivamus sodales inceptos risus feugiat justo finibus. Risus eu feugiat eros pharetra est consectetur montes parturient. Hendrerit consequat quis lacinia imperdiet cubilia cras eget porta. Integer nulla senectus congue tempus justo sed litora libero suspendisse. Est mattis posuere nam aptent sit imperdiet cras auctor. Sollicitudin nibh eros vulputate eget libero in mauris. Turpis inceptos porta eu magna nascetur cubilia curae lectus. Nisl habitasse dignissim integer amet molestie fringilla dictumst fusce molestie. Eget ad purus vel luctus non facilisis. Id felis inceptos pellentesque curae sodales potenti. Vestibulum semper mollis vestibulum nibh tempor pretium ultricies. Blandit himenaeos cubilia lacinia dapibus, et quam neque. Magna ultrices mauris vel at commodo mus rutrum. Viverra nunc lorem ligula pulvinar phasellus tincidunt placerat elementum. Enim purus senectus potenti vestibulum sem conubia cras lacus mollis. Ultricies accumsan facilisis pulvinar donec risus eleifend ut. At suspendisse integer tincidunt venenatis mattis, semper sed hac. Habitant cras placerat nam semper aenean lorem placerat. Quisque feugiat aptent pulvinar non suspendisse a taciti blandit. Dui venenatis consectetur curae maximus elit arcu ridiculus. Faucibus ullamcorper tempus rhoncus; blandit elementum dignissim. Volutpat lacinia sed rhoncus mauris potenti aenean natoque. Morbi varius ultrices ac rutrum, ultrices enim mollis. Pharetra curae pharetra posuere sociosqu netus. Dis interdum elementum vulputate dis, hac morbi lacus. Semper dictum ridiculus, aptent laoreet potenti malesuada. Eleifend venenatis nascetur eros cras etiam cubilia placerat. Luctus sollicitudin elementum curae a, aliquet ex purus habitant. Posuere quis leo natoque nec varius potenti suspendisse est! Efficitur mollis senectus nunc dignissim adipiscing laoreet dictum potenti. Odio vivamus porttitor sem commodo habitasse. Feugiat adipiscing lorem est consequat interdum; pharetra fusce dictum. Condimentum sem nec finibus fermentum sollicitudin tortor. Vehicula accumsan porttitor amet aliquam justo fermentum neque morbi. Pulvinar libero ligula in duis mi congue. Euismod turpis penatibus porta cubilia penatibus euismod. Primis consequat finibus orci aenean gravida ante. Eros hac et placerat ac penatibus, vestibulum quisque aptent. Vehicula hendrerit turpis dapibus odio hendrerit maecenas. Hac etiam cursus quis, ipsum quam pharetra facilisis aliquam."
                        }""")
                .exchange()
        //then
                .expectStatus().isBadRequest()
                .expectHeader().doesNotExist(HttpHeaders.LOCATION)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .json("""
                        {
                            "errors": [
                                "Товар не указан",
                                "Оценка меньше 1",
                                "Размер отзыва не должен превышать 1000 символов"
                            ]
                        }""");
    }

    @Test
    void findProductReviewsByProductId_UserIsNotAuthenticated_ReturnsNotAuthorized() {
        //given

        //when

        //then
        this.webTestClient
                .get()
                .uri("/feedback-api/product-reviews/by-product-id/1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}