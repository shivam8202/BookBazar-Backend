package com.bookbazaar.service;

import com.bookbazaar.domain.*;
import com.bookbazaar.dto.*;
import com.bookbazaar.exception.*;
import com.bookbazaar.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    private static final double GST = 0.05;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 1000.0;

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }

        
        Map<Long, Book> lockedBooks = new HashMap<>();
        for (OrderItemRequest it : req.getItems()) {
            Book b = bookRepository.findByIdForUpdate(it.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + it.getBookId()));
            if (it.getQuantity() == null || it.getQuantity() <= 0)
                throw new BadRequestException("Quantity must be positive");
            if (b.getStock() < it.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for book id " + b.getId());
            }
            lockedBooks.put(b.getId(), b);
        }

        double subtotal = 0.0;
        Order order = Order.builder().user(user).status(OrderStatus.PLACED).build();

        for (OrderItemRequest it : req.getItems()) {
            Book b = lockedBooks.get(it.getBookId());
            double line = b.getPrice() * it.getQuantity();
            subtotal += line;

            OrderItem oi = OrderItem.builder()
                    .book(b)
                    .quantity(it.getQuantity())
                    .priceAtPurchase(b.getPrice())
                    .build();
            order.addItem(oi);
        }

        double gst = subtotal * GST;
        double discount = (subtotal > DISCOUNT_THRESHOLD) ? (subtotal * DISCOUNT_RATE) : 0.0;
        double total = subtotal + gst - discount;
        order.setTotalAmount(roundTwoDecimals(total));

        orderRepository.save(order);

        
        for (OrderItem oi : order.getItems()) {
            Book b = oi.getBook();
            b.setStock(b.getStock() - oi.getQuantity());
            bookRepository.save(b);
        }

        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new BadRequestException("Only placed orders can be cancelled");
        }

        for (OrderItem oi : order.getItems()) {
            Book b = bookRepository.findByIdForUpdate(oi.getBook().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + oi.getBook().getId()));
            b.setStock(b.getStock() + oi.getQuantity());
            bookRepository.save(b);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        return toResponse(order);
    }

    public Page<OrderResponse> getOrdersByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findByUser(user, pageable).map(this::toResponse);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(oi -> OrderItemResponse.builder()
                        .bookId(oi.getBook().getId())
                        .title(oi.getBook().getTitle())
                        .quantity(oi.getQuantity())
                        .priceAtPurchase(oi.getPriceAtPurchase())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private double roundTwoDecimals(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
