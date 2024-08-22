package ru.zinin.catalogue.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.zinin.catalogue.entity.Product;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("/sql/products.sql")
class ProductRepositoryIT {

    @Autowired
    ProductRepository productRepository;

    @Test
    void findAllByTitleLikeIgnoreCase_ReturnsFilteredProductsList() {
        //given
        var filter = "%шоколадка%";;
        //when
        var products = this.productRepository.findAllByTitleLikeIgnoreCase(filter);
        //then
        assertEquals(List.of(new Product(2, "Шоколадка", "Очень вкусная шоколадка")), products);
    }
}