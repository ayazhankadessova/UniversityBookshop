PROMPT DROP TABLES;

DROP TABLE Book CASCADE CONSTRAINT;
DROP TABLE Student CASCADE CONSTRAINT;
DROP TABLE Orders CASCADE CONSTRAINT;

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

CREATE TABLE Orders (
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
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES Book(book_id)
);

-- book alr exists

ALTER TABLE Orders_Book
ADD CONSTRAINT FK_ORDERS_BOOK_ORDER_ID
FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE;

COMMIT;

PROMPT INSERT Book TABLE;

INSERT INTO Book VALUES (1, 'To Kill a Mockingbird', 'Harper Lee', 12.99, 50);
INSERT INTO Book VALUES (2, '1984', 'George Orwell', 10.99, 30);
INSERT INTO Book VALUES (3, 'Pride and Prejudice', 'Jane Austen', 8.99, 40);
INSERT INTO Book VALUES (4, 'The Great Gatsby', 'F. Scott Fitzgerald', 11.99, 20);
INSERT INTO Book VALUES (5, 'One Hundred Years of Solitude', 'Gabriel García Márquez', 9.99, 60);

-- Insert into Student table
PROMPT INSERT Student TABLE;
INSERT INTO Student VALUES (1, 'John Smith', 'Male', 'English', 50, 0.05);
INSERT INTO Student VALUES (2, 'Sarah Johnson', 'Female', 'Biology', 75, 0.10);
INSERT INTO Student VALUES (3, 'David Chen', 'Male', 'Computer Science', 100, 0.15);
INSERT INTO Student VALUES (4, 'Emily Wong', 'Female', 'History', 25, 0.02);

-- Insert into Orders table
PROMPT INSERT Orders TABLE;
INSERT INTO Orders VALUES (1, 1, '2023-03-29', 40.00, 'Credit Card', '1234567890123456');
INSERT INTO Orders VALUES (2, 3, '2023-03-30', 30.00, 'Debit Card', '6543210987654321');
INSERT INTO Orders VALUES (3, 2, '2023-03-30', 50.00, 'Credit Card', '9876543210123456');
INSERT INTO Orders VALUES (4, 4, '2023-03-31', 20.00, 'PayPal', NULL);


-- Insert into Orders_Book table
PROMPT INSERT Orders_Book TABLE;
INSERT INTO Orders_Book VALUES (1, 1, 2, '2023-03-02');
INSERT INTO Orders_Book VALUES (1, 3, 1, '2023-03-02');
INSERT INTO Orders_Book VALUES (2, 2, 3, '2023-03-01');
INSERT INTO Orders_Book VALUES (3, 4, 2, '2023-03-03');
INSERT INTO Orders_Book VALUES (4, 1, 1, '2023-03-01');
INSERT INTO Orders_Book VALUES (4, 2, 1, '2023-03-01');

-- COMMIT;

-- SET AUTOCOMMIT ON
