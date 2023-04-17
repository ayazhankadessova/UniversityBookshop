# University BookShop Manager


<h2 align="center">
 <img src="https://media.giphy.com/media/58lsLv6YlFtOOM3iob/giphy.gif" width="70">LEarnBU: Web-based Financial Literacy Game<img src="https://media.giphy.com/media/58lsLv6YlFtOOM3iob/giphy.gif" width="70">
</h2>

## Group Members:

- KADESSOVA Ayazhan 
- ONG Jun Kye
- SHAMMO Shahtiya Khan
- LIM Jihye

## About

This is a **`Java-based university bookshop database application`** developed for **COMP2016 Database Management** course.

It supports various functions such as searching for orders, updating orders, placing new orders, canceling orders, and displaying all available books and orders.

The application uses JDBC driver to access an Oracle database and is implemented with **PL/SQL** triggers to enforce various rules.

_Feel free to download and use this application as a reference for your own database projects._

## ER Diagram

<p align="center">
  <img src="ER_diagram.png" width="800" title="ER Diagram">
</p>

## Setup

1. Before running the program, make sure to execute the sql files in your **`Oracle database`** to drop tables & create the necessary tables, triggers, and initial values for the Book and Student tables.

- Terminal:

`@ ./group3_dbdrop.sql`

`@ ./group3_dbinsert.sql`

2. Change your login and password for Oracle Database in UniversityBookshop.java:

```
public boolean loginDB() {
		String username = "yourUSERNAME";// Replace to your username
		String password = "yourPASSWORD";// Replace to your password
```

## Usage

To run the program, simply compile and run UniversityBookshop.java. You will be prompted to select an option from the menu to perform the desired operation. Follow the on-screen instructions to complete each operation.

## Options

1. 🔍 Search Order by OrderID: This option allows the manager to search for a specific order by its unique order ID.

2. 🔍👩‍💻 Search Order by StudentID: This option allows the manager to search for all orders placed by a specific student, identified by their student ID.

3. 🔍📚 Update Order for Student: This option allows the manager to update an existing order for a specific student, such as modifying the delivery date or adding/removing books.

4. 🛍️ Place an Order: This option allows the student to place a new order specifying the books and delivery date. Books in one order can have different delivery dates. Delivery date for every book ranges between 3-14 days.

   A student can place an order if the following conditions are met:

   - No books in the order are out of stock.
   - The student does not have any outstanding orders (all books ordered earlier had been delivered).

   After an order is confirmed, the total price of the order should be calculated automatically based on the book prices and the current discount level.

   - If payment method is credit card, credit card no is required.

5. 🗑️ Cancel an Order: This option allows the manager to cancel an existing order, provided that no books from the order have been delivered.

   A student can cancel an order if the following conditions are met:

   - None of the books in the order has been delivered.
   - The order was made in the recent 7 days.

6. 📚 Show All Books: This option displays a list of all available books in the inventory.

7. 📋 Show All Orders: This option displays a list of all orders placed in the system, along with their details such as the order ID, student ID, and delivery date.

8. 🎁 Check Discount for StudentID: This option allows the manager to check the discount for the student with the given student ID.

9. Exit: This option allows the manager to exit the system.

With these options, the University BookShop manager can effectively manage the book inventory, process orders, and provide timely and efficient service to its customers.

## Issues/need to check

1. cancel order trigger - resolved
2. remove sql prints - resolved
3. doesnt show payment method - resolved
4. decide on delivery date - resolved
5. after placing order, options printec twice [resolved, issue: declared local scanner]

!! important : use bfg repo-cleaner for removing password
