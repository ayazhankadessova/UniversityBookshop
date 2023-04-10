CREATE TABLE Book (
    book_id INT,
    title VARCHAR(30) NOT NULL,
    author CHAR(30) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    amount INT NOT NULL,
    PRIMARY KEY (book_id));

CREATE TABLE Student (
    student_id INT,
    name VARCHAR(30) NOT NULL,
    gender VARCHAR(30) NOT NULL,
    major VARCHAR(30) NOT NULL,
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
    order_delivered VARCHAR(20) DEFAULT 'pending',
    PRIMARY KEY (order_id),
    FOREIGN KEY (student_id) REFERENCES Student(student_id)
);


CREATE TABLE Orders_Book (
    order_id INT,
    book_id INT,
    book_amount INT,
    delivery_date DATE NOT NULL,
    PRIMARY KEY (order_id, book_id),
    FOREIGN KEY (book_id) REFERENCES Book(book_id)
);

COMMIT;