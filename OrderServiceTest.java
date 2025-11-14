package com.bookbazaar;

import com.bookbazaar.domain.*;
import com.bookbazaar.dto.*;
import com.bookbazaar.exception.InsufficientStockException;
import com.bookbazaar.repository.*;
import com.bookbazaar.service.OrderService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
public class OrderServiceTest {

    @Autowired OrderService orderService;
    @Autowired BookRepository bookRepository;
    @Autowired UserRepository userRepository;

    private User user;
    private Book book;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        bookRepository.deleteAll();

        user = User.builder()
                .name("Test User")
                .email("user" + System.currentTimeMillis() + "@test.com")
                .build();
        userRepository.save(user);

        book = Book.builder()
                .title("Java 101")
                .author("A")
                .category("Programming")
                .price(500.0)
                .stock(5)
                .build();
        bookRepository.save(book);
    }

    @Test
    void placeOrder_success_reducesStock() {
        PlaceOrderRequest req = PlaceOrderRequest.builder()
                .userId(user.getId())
                .items(java.util.List.of(
                        OrderItemRequest.builder().bookId(book.getId()).quantity(2).build()
                ))
                .build();

        var resp = orderService.placeOrder(req);
        assertNotNull(resp.getId());
        Book updated = bookRepository.findById(book.getId()).orElseThrow();
        assertEquals(3, updated.getStock());
    }

    @Test
    void placeOrder_insufficientStock_throws() {
        PlaceOrderRequest req = PlaceOrderRequest.builder()
                .userId(user.getId())
                .items(java.util.List.of(
                        OrderItemRequest.builder().bookId(book.getId()).quantity(999).build()
                ))
                .build();

        assertThrows(InsufficientStockException.class, () -> orderService.placeOrder(req));
    }
}
