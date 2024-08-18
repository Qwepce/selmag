package ru.zinin.manager.client;

import ru.zinin.manager.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductsRestClient {

    List<Product> findAllProducts(String filter);

    Product createProduct(String title, String details);

    Optional<Product> findProduct(int id);

    void updateProduct(int productId, String title, String details);

    void deleteProduct(int productId);
}
