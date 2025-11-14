package com.bookbazaar.dto;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItemRequest {
    private Long bookId;
    private Integer quantity;
}
