package ru.zinin.catalogue.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.zinin.catalogue.entity.Product;


public interface ProductRepository extends CrudRepository<Product, Integer> {


    // select * from catalogue.t_product where c_title ilike :filter
    /* JPQL-запрос
    @Query(value = "select p from Product p where p.title ilike :filter") */
    /* SQL-запрос */
    @Query(value = "select * from catalogue.t_product where c_title ilike :filter", nativeQuery = true)
    Iterable<Product> findAllByTitleLikeIgnoreCase(@Param("filter") String filter);
}
