package ru.zinin.customer.entity;

import java.util.UUID;

public record ProductReview(
        UUID id,
        Integer productId,
        int rating,
        String review
) {
}
