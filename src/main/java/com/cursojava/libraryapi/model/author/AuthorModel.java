package com.cursojava.libraryapi.model.author;

import com.cursojava.libraryapi.model.global.AuditableModel;
import com.cursojava.libraryapi.model.book.BookModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "authors")
@Getter
@Setter
public class AuthorModel extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private LocalDate birthdate;

    private String nationality;

    // 1 autor tem muitos livros
    @OneToMany(mappedBy = "author")
    private List<BookModel> books;

    @Override
    public String toString() {
        return "AuthorModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", birthdate=" + birthdate +
                ", nationality='" + nationality + '\'' +
                '}';
    }
}
