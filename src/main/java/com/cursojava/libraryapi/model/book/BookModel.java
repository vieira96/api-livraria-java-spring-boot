package com.cursojava.libraryapi.model.book;

import com.cursojava.libraryapi.model.global.AuditableModel;
import com.cursojava.libraryapi.model.author.AuthorModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "books")
@Getter
@Setter
public class BookModel extends AuditableModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String isbn;

    private String title;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @Enumerated(EnumType.STRING)
    private BookGender gender;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private AuthorModel author;

    @Override
    public String toString() {
        return "BookModel{" +
                "id=" + id +
                ", isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", publishDate=" + publishDate +
                ", gender=" + gender +
                ", price=" + price +
                '}';
    }
}
