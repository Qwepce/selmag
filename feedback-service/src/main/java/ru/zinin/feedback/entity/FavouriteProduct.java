package ru.zinin.feedback.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("favourite_product")
public class FavouriteProduct {

    @Id
    private UUID id;

    private Integer productId;

    private String userId;
}
