ALTER TABLE Orders_Book
ADD CONSTRAINT FK_ORDERS_BOOK_ORDER_ID
FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE;

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

-- Create a trigger to update the amount of books in the Book table after we Insert it to Orders_Book table
CREATE OR REPLACE TRIGGER update_total_price_and_amount
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
v_book_amount NUMBER;
BEGIN
v_book_amount := (SELECT book_amount FROM Orders_Book WHERE order_id = :OLD.order_id AND book_id = :OLD.book_id);

-- Add book amount back
UPDATE Book
SET amount = amount + v_book_amount
WHERE book_id = :OLD.book_id;
END;
/


COMMIT;

SET AUTOCOMMIT ON
