CREATE TABLE books (
    id UUID PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    publish_date DATE,
    gender VARCHAR(30) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    author_id UUID NOT NULL,
    CONSTRAINT fk_book_author
        FOREIGN KEY (author_id) REFERENCES authors (id),
    CONSTRAINT chk_book_gender
        CHECK (gender IN (
            'FICCAO',
            'ROMANCE',
            'FANTASIA',
            'MISTERIO',
            'BIOGRAFIA',
            'TECNICO',
            'OUTROS'
        ))
);

CREATE INDEX idx_book_id_author ON books (author_id);
