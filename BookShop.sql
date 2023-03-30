PROMPT DROP TABLES;

DROP TABLE Book CASCADE CONSTRAINT;
DROP TABLE Student CASCADE CONSTRAINT;
DROP TABLE OrdersNew CASCADE CONSTRAINT;

CREATE TABLE Book (
    book_id INT,
    title CHAR(255) NOT NULL,
    author CHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    amount INT NOT NULL,
    PRIMARY KEY (book_id)
);

CREATE TABLE Student (
    student_id INT,
    name VARCHAR(255) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    major VARCHAR(255) NOT NULL,
    total_spent INTEGER,
    discount DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (student_id)
);

CREATE TABLE OrdersNew (
    order_id INT,
    student_id INT NOT NULL,
    order_date DATE NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    card_no VARCHAR(16),
    PRIMARY KEY (order_id),
    FOREIGN KEY (student_id) REFERENCES Student(student_id)
);

CREATE TABLE Orders_Book (
    order_id INT,
    book_id INT,
    book_amount INT,
    delivery_date DATE NOT NULL,
    PRIMARY KEY (order_id, book_id),
    FOREIGN KEY (order_id) REFERENCES OrdersNew(order_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES Book(book_id)
);


ALTER TABLE Orders_Book
ADD CONSTRAINT FK_ORDERS_BOOK_ORDER_ID
FOREIGN KEY (order_id) REFERENCES OrdersNew(order_id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED NULL;


COMMIT;

PROMPT INSERT Book TABLE;

INSERT INTO Book VALUES (1, 'To Kill a Mockingbird', 'Harper Lee', 12.99, 50);
INSERT INTO Book VALUES (2, '1984', 'George Orwell', 10.99, 30);
INSERT INTO Book VALUES (3, 'Pride and Prejudice', 'Jane Austen', 8.99, 40);
INSERT INTO Book VALUES (4, 'The Great Gatsby', 'F. Scott Fitzgerald', 11.99, 20);
INSERT INTO Book VALUES (5, 'One Hundred Years of Solitude', 'Gabriel García Márquez', 9.99, 60);


-- COMMIT;

-- SET AUTOCOMMIT ON
