PROMPT DROP TABLES;

DROP TABLE Book CASCADE CONSTRAINT;
DROP TABLE Student CASCADE CONSTRAINT;
DROP TABLE Orders CASCADE CONSTRAINT;
DROP TABLE Orders_Book CASCADE CONSTRAINT;

COMMIT;


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

-- book alr exists

-- ALTER TABLE Orders_Book
-- ADD CONSTRAINT FK_ORDERS_BOOK_BOOK_ID
-- FOREIGN KEY (book_id) REFERENCES Book(book_id) INITIALLY DEFERRED DEFERRABLE;

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
INSERT INTO Student VALUES (1, 'John Smith', 'Male', 'English', 0.05);
INSERT INTO Student VALUES (2, 'Sarah Johnson', 'Female', 'Biology', 0.10);
INSERT INTO Student VALUES (3, 'David Chen', 'Male', 'Computer Science', 0.15);
INSERT INTO Student VALUES (4, 'Emily Wong', 'Female', 'History', 0.02);

-- Insert into Orders table
-- PROMPT INSERT Orders TABLE;
-- INSERT INTO Orders VALUES (1, 1, '29-MAR-2023', 40.00, 'Credit Card', '1234567890123456');
-- INSERT INTO Orders VALUES (2, 3, '30-MAR-2023', 30.00, 'Debit Card', '6543210987654321');
-- INSERT INTO Orders VALUES (3, 2, '30-MAR-2023', 50.00, 'Credit Card', '9876543210123456');
-- INSERT INTO Orders VALUES (4, 4, '31-MAR-2023', 20.00, 'PayPal', NULL);

-- Insert into Orders_Book table
-- DD-MON-YYYY' (e.g., 23-MAR-2022), 
-- PROMPT INSERT Orders_Book TABLE;
-- INSERT INTO Orders_Book VALUES (1, 1, 2, '02-MAR-2023');
-- INSERT INTO Orders_Book VALUES (1, 3, 1, '02-MAR-2023');
-- INSERT INTO Orders_Book VALUES (2, 2, 3, '01-MAR-2023');
-- INSERT INTO Orders_Book VALUES (3, 4, 2, '03-MAR-2023');
-- INSERT INTO Orders_Book VALUES (4, 1, 1, '01-MAR-2023');
-- INSERT INTO Orders_Book VALUES (4, 2, 1, '01-MAR-2023');


--up to here

-- done
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

-- done
-- Create a trigger to update the total price of the order in the Orders table after we Insert a new row into the Orders_Book table
CREATE OR REPLACE TRIGGER update_total_price_and_amount 
AFTER INSERT ON Orders_Book
FOR EACH ROW
DECLARE
  v_book_amount NUMBER;
  v_order_count NUMBER;
BEGIN
  v_book_amount := :NEW.book_amount;

  -- Check if the order exists
  SELECT COUNT(*) INTO v_order_count FROM Orders WHERE order_id = :NEW.order_id;

  -- -- Insert into Orders table if it doesn't exist
  -- IF v_order_count = 0 THEN
  --   INSERT INTO Orders
  --   VALUES (:NEW.order_id, 2, SYSDATE, 0, 'CASH', NULL);
  -- END IF;

  -- -- -- -- Update total price in Orders table
  -- UPDATE Orders
  -- SET total_price = total_price + (SELECT SUM(Book.price * v_book_amount)
  --                    FROM Book
  --                    WHERE Book.book_id = :NEW.book_id)
  -- WHERE order_id = :NEW.order_id;

  -- Update book amount
  UPDATE Book
  SET amount = amount - v_book_amount
  WHERE book_id = :NEW.book_id;
END;
/


-- Create a trigger to check if we can cancel order
CREATE OR REPLACE TRIGGER check_order_cancel
BEFORE DELETE ON Orders
FOR EACH ROW
DECLARE
    order_age NUMBER;
    book_delivered NUMBER;
    v_order_date DATE;
BEGIN

    order_age := :OLD.order_date - SYSDATE;

    IF order_age > 7 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Cannot cancel order: order was made more than 7 days ago');
    END IF;
    
    -- Check if any book has been delivered
    SELECT COUNT(*) INTO book_delivered
    FROM Orders_Book
    WHERE order_id = :OLD.order_id AND delivery_date > SYSDATE;

    IF book_delivered > 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Cannot cancel order: some books have already been delivered');
    END IF;
END;
/

-- to check again
-- Create a trigger to update the total price of the order in the Orders table after we Delete a row from the Orders_Book table
-- and update the amount of the book in the Book table
-- and update the discount of the student in the Student table
--hey
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



-- Create a trigger to update the discount of the student in the Student table after we Insert a new row into the Orders table or Delete
-- CREATE OR REPLACE TRIGGER update_total_spent
-- AFTER INSERT ON Orders
-- FOR EACH ROW
-- DECLARE
--     year INT;
--     total DECIMAL(10,2);
--     discount DECIMAL(10,2);
-- BEGIN
--     year := EXTRACT(YEAR FROM :NEW.order_date);
--     SELECT SUM(total_price) INTO total
--     FROM Orders
--     WHERE student_id = :NEW.student_id AND EXTRACT(YEAR FROM order_date) = year;

--     CASE
--         WHEN total > 2000 THEN discount := 0.20;
--         WHEN total > 1000 THEN discount := 0.10;
--         ELSE discount := 0;
--     END CASE;

--     UPDATE Student
--     SET Student.discount = discount
--     WHERE student_id = :NEW.student_id;
-- END;
-- /

-- CREATE OR REPLACE TRIGGER update_total_spent
-- AFTER INSERT OR DELETE ON Orders
-- FOR EACH ROW
-- BEGIN
--   update_student_discount(:OLD.student_id);
--   update_student_discount(:NEW.student_id);
-- END;
-- /

-- CREATE OR REPLACE PROCEDURE update_student_discount(student_id IN NUMBER) AS
--   year INT;
--   total DECIMAL(10,2);
--   discount DECIMAL(10,2);
-- BEGIN
--   year := EXTRACT(YEAR FROM SYSDATE);
--   SELECT SUM(total_price) INTO total
--   FROM Orders
--   WHERE student_id = student_id AND EXTRACT(YEAR FROM order_date) = year AND order_id != :NEW.order_id;

--   IF total IS NOT NULL THEN
--     IF total > 2000 THEN
--       discount := 0.20;
--     ELSIF total > 1000 THEN
--       discount := 0.10;
--     ELSE
--       discount := 0;
--     END IF;

--     UPDATE Student
--     SET discount = discount
--     WHERE student_id = student_id;
--   END IF;
-- END;
-- /

UPDATE Student
SET discount = (
  CASE
    WHEN (
      SELECT COALESCE(SUM(total_price), 0)
      FROM Orders
      WHERE student_id = 1
      AND EXTRACT(YEAR FROM order_date) = EXTRACT(YEAR FROM CURRENT_DATE)
    ) > 2000 THEN 0.20
    WHEN (
      SELECT COALESCE(SUM(total_price), 0)
      FROM Orders
      WHERE student_id = 1
      AND EXTRACT(YEAR FROM order_date) = EXTRACT(YEAR FROM CURRENT_DATE)
    ) > 1000 THEN 0.10
    ELSE 0
  END
)
WHERE student_id = 1;



-- COMMIT;

-- SET AUTOCOMMIT ON
