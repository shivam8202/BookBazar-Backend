package com.bookbazaar;



import com.bookbazaar.domain.Book;
import com.bookbazaar.domain.User;
import com.bookbazaar.dto.OrderItemRequest;
import com.bookbazaar.dto.PlaceOrderRequest;
import com.bookbazaar.exception.InsufficientStockException;
import com.bookbazaar.repository.BookRepository;
import com.bookbazaar.repository.UserRepository;
import com.bookbazaar.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderServiceConcurrencyTest {

    @Autowired
    OrderService orderService;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void twoUsersOrderingSameBook_concurrencyTest() throws Exception {

      
        User u1 = userRepository.save(User.builder().name("U1").email("u1@test.com").build());
        User u2 = userRepository.save(User.builder().name("U2").email("u2@test.com").build());

       
        Book book = bookRepository.save(
                Book.builder()
                        .title("Java Book")
                        .author("Author A")
                        .price(500.0)
                        .stock(5)
                        .build()
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<String> orderTask1 = () -> {
            PlaceOrderRequest req = PlaceOrderRequest.builder()
                    .userId(u1.getId())
                    .items(List.of(
                            OrderItemRequest.builder().bookId(book.getId()).quantity(3).build()
                    ))
                    .build();
            try {
                orderService.placeOrder(req);
                return "SUCCESS";
            } catch (InsufficientStockException e) {
                return "FAILED";
            }
        };

        Callable<String> orderTask2 = () -> {
            PlaceOrderRequest req = PlaceOrderRequest.builder()
                    .userId(u2.getId())
                    .items(List.of(
                            OrderItemRequest.builder().bookId(book.getId()).quantity(3).build()
                    ))
                    .build();
            try {
                orderService.placeOrder(req);
                return "SUCCESS";
            } catch (InsufficientStockException e) {
                return "FAILED";
            }
        };

        Future<String> f1 = executor.submit(orderTask1);
        Future<String> f2 = executor.submit(orderTask2);

        String result1 = f1.get(); // waits for completion
        String result2 = f2.get();

        executor.shutdown();

        
        assertTrue(result1.equals("SUCCESS") ^ result2.equals("SUCCESS"),
                "Exactly one order must succeed");

        
        Book updated = bookRepository.findById(book.getId()).orElseThrow();
        assertTrue(updated.getStock() >= 0, "Stock must never go negative");


        System.out.println("Order1: " + result1);
        System.out.println("Order2: " + result2);
        System.out.println("Final stock: " + updated.getStock());
    }
}
