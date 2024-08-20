package ru.zinin.manager.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.zinin.manager.client.BadRequestException;
import ru.zinin.manager.client.ProductsRestClient;
import ru.zinin.manager.controller.payload.NewProductPayload;
import ru.zinin.manager.entity.Product;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("catalogue/products")
@Slf4j
public class ProductsController {

    private final ProductsRestClient productsRestClient;

    @GetMapping("list")
    public String getAllProducts(Model model, @RequestParam(value = "filter", required = false) String filter) {
        model.addAttribute("products", this.productsRestClient.findAllProducts(filter));
        model.addAttribute("filter", filter);
        return "catalogue/products/list";
    }

    @GetMapping("create")
    public String getNewProductPage() {
        return "catalogue/products/new_product";
    }

    @PostMapping
    public String createProduct(NewProductPayload payload, Model model) {

        try {
            Product product = this.productsRestClient.createProduct(payload.title(), payload.details());

            return "redirect:/catalogue/products/%d".formatted(product.id());
        } catch (BadRequestException exception) {
            model.addAttribute("payload", payload);
            model.addAttribute("errors", exception.getErrors());
            return "catalogue/products/new_product";
        }
    }

}
