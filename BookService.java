package com.bookbazaar.service;

import com.bookbazaar.domain.Book;
import com.bookbazaar.dto.BookDto;
import com.bookbazaar.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    public Page<BookDto> search(String keyword, String category, Pageable pageable) {
        Specification<Book> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("author")), like)
                ));
            }
            if (category != null && !category.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return bookRepository.findAll(spec, pageable).map(this::toDto);
    }

    private BookDto toDto(Book b) {
        return BookDto.builder()
                .id(b.getId())
                .title(b.getTitle())
                .author(b.getAuthor())
                .category(b.getCategory())
                .price(b.getPrice())
                .stock(b.getStock())
                .build();
    }
}
