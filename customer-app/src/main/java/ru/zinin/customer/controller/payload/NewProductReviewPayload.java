package ru.zinin.customer.controller.payload;


public record NewProductReviewPayload(
        Integer rating,
        String review
) {
}
