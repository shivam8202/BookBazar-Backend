package com.bookbazaar.dto;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItemResponse {
    private Long bookId;
    private String title;
    private Integer quantity;
    private Double priceAtPurchase;
}
