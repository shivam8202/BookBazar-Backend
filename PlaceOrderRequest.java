package com.bookbazaar.dto;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlaceOrderRequest {
    private Long userId;
    private List<OrderItemRequest> items;
}
