package com.bookbazaar.controller;

import com.bookbazaar.dto.*;
import com.bookbazaar.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public OrderResponse placeOrder(@RequestBody PlaceOrderRequest req) {
        return orderService.placeOrder(req);
    }

    @PutMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    @GetMapping("/user/{userId}")
    public Page<OrderResponse> getOrdersByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return orderService.getOrdersByUser(userId, PageRequest.of(page, size));
    }
}
