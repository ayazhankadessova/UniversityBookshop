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

CREATE TABLE Orders_Total (
    order_id INT,
    student_id INT NOT NULL,
    order_date DATE NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
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

ALTER TABLE Orders_Book
ADD CONSTRAINT FK_ORDERS_BOOK_ORDER_ID
FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE;


COMMIT;

-- dont add on delete cascade because we delete in the trigger update_student_discount_del
ALTER TABLE Orders_Total 
ADD CONSTRAINT FK_ORDERS_Total_ORDER_ID
FOREIGN KEY (order_id) REFERENCES Orders(order_id) INITIALLY DEFERRED DEFERRABLE;

COMMIT;

PROMPT INSERT Book TABLE;

INSERT INTO Book VALUES (1, 'To Kill a Mockingbird', 'Harper Lee', 180.99, 50);
INSERT INTO Book VALUES (2, '1984', 'George Orwell', 200.05, 30);
INSERT INTO Book VALUES (3, 'Pride and Prejudice', 'Jane Austen', 220.05, 40);
INSERT INTO Book VALUES (4, 'The Great Gatsby', 'F. Scott Fitzgerald', 290.05, 20);
INSERT INTO Book VALUES (5, 'One Hundred Years of Solitude', 'Gabriel García Márquez', 300.05, 60);
INSERT INTO Book VALUES (6, 'The Catcher in the Rye', 'J.D. Salinger', 240.99, 35);
INSERT INTO Book VALUES (7, 'The Lord of the Rings', 'J.R.R. Tolkien', 350.99, 25);
INSERT INTO Book VALUES (8, 'Brave New World', 'Aldous Huxley', 180.99, 30);
INSERT INTO Book VALUES (9, 'Animal Farm', 'George Orwell', 150.05, 50);
INSERT INTO Book VALUES (10, 'The Hobbit', 'J.R.R. Tolkien', 170.05, 45);

-- Insert into Student table
PROMPT INSERT Student TABLE;
INSERT INTO Student VALUES (1, 'John Smith', 'Male', 'English', 0.00);
INSERT INTO Student VALUES (2, 'Sarah Johnson', 'Female', 'Biology', 0.00);
INSERT INTO Student VALUES (3, 'David Chen', 'Male', 'Computer Science', 0.00);
INSERT INTO Student VALUES (4, 'Emily Wong', 'Female', 'History', 0.00);
INSERT INTO Student VALUES (5, 'Michael Kim', 'Male', 'Mathematics', 0.00);
INSERT INTO Student VALUES (6, 'Jessica Lee', 'Female', 'Chemistry', 0.00);
INSERT INTO Student VALUES (7, 'Daniel Rodriguez', 'Male', 'Physics', 0.00);
INSERT INTO Student VALUES (8, 'Avery Taylor', 'Female', 'Psychology', 0.00);
INSERT INTO Student VALUES (9, 'Kevin Patel', 'Male', 'Economics', 0.00);
INSERT INTO Student VALUES (10, 'Sophia Kim', 'Female', 'Sociology', 0.00);

COMMIT;


-- to check again
-- Check if there is credit card when payment option is credit card
CREATE OR REPLACE TRIGGER check_credit_card
BEFORE INSERT ON Orders
FOR EACH ROW
BEGIN
  -- Check if payment method is credit card and card number is valid
  IF :NEW.payment_method = 'Credit Card' AND ( :NEW.card_no IS NULL ) THEN
    -- Raise an error if the card number is not valid
    RAISE_APPLICATION_ERROR(-20001, 'Invalid credit card number');
  END IF;
END;
/

-- Create a trigger to check if there is enough amount of the book in the Book table Before we Insert it to Orders_Book table
CREATE OR REPLACE TRIGGER check_book_amount
BEFORE INSERT ON Orders_Book
FOR EACH ROW
DECLARE
  book_amount NUMBER;
BEGIN
  -- Check if there is enough amount of the book in the Book table
  SELECT amount INTO book_amount FROM Book WHERE book_id = :NEW.book_id;
  IF :NEW.book_amount > book_amount THEN
    -- Raise an error if there is not enough amount of the book
    RAISE_APPLICATION_ERROR(-20001, 'Not enough books');
  END IF;
END;
/

-- update student discount after new order
CREATE OR REPLACE TRIGGER update_student_discount
AFTER INSERT OR UPDATE ON Orders
FOR EACH ROW
DECLARE
  v_total_price DECIMAL(10,2);
  v_total_new DECIMAL(10,2):= 0;

BEGIN
  -- initial new
  DBMS_OUTPUT.PUT_LINE('v_total_new=' || v_total_new);

  SELECT SUM(total_price)
  INTO v_total_price
  FROM Orders_Total
  WHERE student_id = :NEW.student_id
    AND order_date >= ADD_MONTHS(TRUNC(SYSDATE, 'YEAR'), -12);

-- check if v_total_price is empty or not.
-- if we try to add empty -> v_total_new becaomes empty and the trigger does not work
-- if v_total_price not empty, we can add
IF v_total_price > 0 THEN
    v_total_new := v_total_price + :NEW.total_price;
  ELSE
    v_total_new := :NEW.total_price;
  END IF;

-- check in terminal
  DBMS_OUTPUT.PUT_LINE('initial new ' || :NEW.total_price);
  DBMS_OUTPUT.PUT_LINE(' after, v_total_new= ' || v_total_new);

  -- set discount
  IF v_total_new > 2000 THEN
    UPDATE Student
    SET discount = 0.20
    WHERE student_id = :NEW.student_id;
  ELSIF v_total_new > 1000 THEN
    UPDATE Student
    SET discount = 0.10
    WHERE student_id = :NEW.student_id;
  ELSE
    UPDATE Student
    SET discount = 0
    WHERE student_id = :NEW.student_id;
  END IF;
END;
/

-- Create a trigger to update the amount of books in the Book table after we Insert it to Orders_Book table
CREATE OR REPLACE TRIGGER update_amount
AFTER INSERT ON Orders_Book
FOR EACH ROW
DECLARE
  v_book_amount NUMBER;
BEGIN
  v_book_amount := :NEW.book_amount;

  -- Update book amount
  UPDATE Book
  SET amount = amount - v_book_amount
  WHERE book_id = :NEW.book_id;
END;
/

-- Create a trigger to update the total price of the order after we delete it from orders_book {when we cancel order}
CREATE OR REPLACE TRIGGER add_book_amount
AFTER DELETE ON Orders_Book
FOR EACH ROW
DECLARE
  v_book_amount INT;
BEGIN
  v_book_amount := :OLD.book_amount;

  -- Add book amount back
  UPDATE Book
  SET amount = amount + v_book_amount
  WHERE book_id = :OLD.book_id;
END;
/

-- update student discount after an order has been deleted
CREATE OR REPLACE TRIGGER update_student_discount_del
AFTER DELETE ON Orders
FOR EACH ROW
DECLARE
  v_total_price DECIMAL(10,2);
  v_total_new DECIMAL(10,2):= 0;

BEGIN
  
  DBMS_OUTPUT.PUT_LINE('v_total_new=' || v_total_new);

  SELECT SUM(total_price)
  INTO v_total_price
  FROM Orders_Total
  WHERE student_id = :OLD.student_id
    AND order_date >= ADD_MONTHS(TRUNC(SYSDATE, 'YEAR'), -12);

-- check if v_total_price is empty or not.
-- if we try to use empty -> v_total_new becomes empty and the trigger does not work
-- if v_total_price not empty, we can use it
  IF v_total_price > 0 THEN
  -- subtract deleted order price
    v_total_new := v_total_price - :OLD.total_price;
  ELSE
  -- if no matches, just 0
    v_total_new := 0;
  END IF;

-- check in terminal
  DBMS_OUTPUT.PUT_LINE('initial total that we are deleting: ' || :OLD.total_price);
  DBMS_OUTPUT.PUT_LINE(' after, v_total_new= ' || v_total_new);

  -- set discount
  IF v_total_new > 2000 THEN
    UPDATE Student
    SET discount = 0.20
    WHERE student_id = :OLD.student_id;
  ELSIF v_total_new > 1000 THEN
    UPDATE Student
    SET discount = 0.10
    WHERE student_id = :OLD.student_id;
  ELSE
    UPDATE Student
    SET discount = 0
    WHERE student_id = :OLD.student_id;
  END IF;

-- delete from Orders_Total
  DELETE FROM Orders_Total WHERE order_id = :OLD.order_id;

END;
/


COMMIT;

SET AUTOCOMMIT ON
