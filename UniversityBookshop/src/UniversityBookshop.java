import java.awt.GridLayout;
import java.util.*;
import java.awt.TextField;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import java.util.Properties;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * This is a flight manager to support: (1) add a flight (2) delete a flight (by
 * flight_no) (3) print flight information (by flight_no) (4) select a flight
 * (by source, dest, stop_no = 0) (5) select a flight (by source, dest, stop_no
 * = 1)
 * 
 * @author comp1160/2016
 */

public class UniversityBookshop {

	Scanner in = null;
	Connection conn = null;
	// Database Host
	final String databaseHost = "orasrv1.comp.hkbu.edu.hk";
	// Database Port
	final int databasePort = 1521;
	// Database name
	final String database = "pdborcl.orasrv1.comp.hkbu.edu.hk";
	final String proxyHost = "faith.comp.hkbu.edu.hk";
	final int proxyPort = 22;
	final String forwardHost = "localhost";
	int forwardPort;
	Session proxySession = null;
	boolean noException = true;

	// JDBC connecting host
	String jdbcHost;
	// JDBC connecting port
	int jdbcPort;

	String[] options = {
			"🔍 Search Order by OrderID",
			"🔍👩‍💻 Search Order by StudentID", // changed to student emoji
			"🔍📚 Update Order for Student",
			"🛍️ Place an Order",
			"🗑️ Cancel an Order",
			"📚 Show All Books",
			"📋 Show All Orders",
			"Exit"
	};

	/**
	 * Get YES or NO. Do not change this function.
	 * 
	 * @return boolean
	 */
	boolean getYESorNO(String message) {
		JPanel panel = new JPanel();
		panel.add(new JLabel(message));
		JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
		JDialog dialog = pane.createDialog(null, "Question");
		dialog.setVisible(true);
		boolean result = JOptionPane.YES_OPTION == (int) pane.getValue();
		dialog.dispose();
		return result;
	}

	/**
	 * Get username & password. Do not change this function.
	 * 
	 * @return username & password
	 */
	String[] getUsernamePassword(String title) {
		JPanel panel = new JPanel();
		final TextField usernameField = new TextField();
		final JPasswordField passwordField = new JPasswordField();
		panel.setLayout(new GridLayout(2, 2));
		panel.add(new JLabel("Username"));
		panel.add(usernameField);
		panel.add(new JLabel("Password"));
		panel.add(passwordField);
		JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
			private static final long serialVersionUID = 1L;

			@Override
			public void selectInitialValue() {
				usernameField.requestFocusInWindow();
			}
		};
		JDialog dialog = pane.createDialog(null, title);
		dialog.setVisible(true);
		dialog.dispose();
		return new String[] { usernameField.getText(), new String(passwordField.getPassword()) };
	}

	/*
	 * Login the proxy. Do not change this function.
	 * 
	 * @return boolean
	 */
	public boolean loginProxy() {
		if (getYESorNO("Using ssh tunnel or not?")) { // if using ssh tunnel
			String[] namePwd = getUsernamePassword("Login cs lab computer");
			String sshUser = namePwd[0];
			String sshPwd = namePwd[1];
			try {
				proxySession = new JSch().getSession(sshUser, proxyHost, proxyPort);
				proxySession.setPassword(sshPwd);
				Properties config = new Properties();
				config.put("StrictHostKeyChecking", "no");
				proxySession.setConfig(config);
				proxySession.connect();
				proxySession.setPortForwardingL(forwardHost, 0, databaseHost, databasePort);
				forwardPort = Integer.parseInt(proxySession.getPortForwardingL()[0].split(":")[0]);
			} catch (JSchException e) {
				e.printStackTrace();
				return false;
			}
			jdbcHost = forwardHost;
			jdbcPort = forwardPort;
		} else {
			jdbcHost = databaseHost;
			jdbcPort = databasePort;
		}
		return true;
	}

	/**
	 * Login the oracle system. Change for your own credentials.
	 * 
	 * @return boolean
	 */
	public boolean loginDB() {
		String username = "***REMOVED***";// Replace to your username
		String password = "***REMOVED***";// Replace to your password

		/* Do not change the code below */
		if (username.equalsIgnoreCase("e1234567") || password.equalsIgnoreCase("e1234567")) {
			String[] namePwd = getUsernamePassword("Login sqlplus");
			username = namePwd[0];
			password = namePwd[1];
		}
		String URL = "jdbc:oracle:thin:@" + jdbcHost + ":" + jdbcPort + "/" + database;

		try {
			System.out.println("Logging " + URL + " ...");
			conn = DriverManager.getConnection(URL, username, password);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Show the options. If you want to add one more option, put into the
	 * options array above.
	 */
	public void showOptions() {
		System.out.println("Please choose following option:");
		for (int i = 0; i < options.length; ++i) {
			System.out.println("(" + (i + 1) + ") " + options[i]);
		}
	}

	/**
	 * Run the manager
	 */
	public void run() {

		// update everytime program starts
		update();
		while (noException) {
			showOptions();
			String line = in.nextLine();
			if (line.equalsIgnoreCase("exit"))
				return;
			int choice = -1;
			try {
				choice = Integer.parseInt(line);
			} catch (Exception e) {
				System.out.println("This option is not available");
				continue;
			}
			if (!(choice >= 1 && choice <= options.length)) {
				System.out.println("This option is not available");
				continue;
			}
			if (choice == 1) {
				orderSearchbyID();
			} else if (choice == 2) {
				orderSearchforStudent("Search");
			} else if (choice == 3) {
				orderSearchforStudent("Update");
			} else if (choice == 4) {
				placeOrder();
			} else if (choice == 5) {
				cancelOrder();
			} else if (choice == 6) {
				displayBooks();
			} else if (choice == 7) {
				listAllOrders();
			} else if (options[choice - 1].equalsIgnoreCase("exit")) {
				break;
			}
		}
	}

	private void orderSearchbyID() {
		System.out.println("Please input order_id or -1 for exit:");

		int order_id = in.nextInt();

		if (order_id == -1)
			return;

		orderSearchbyID(order_id);
	}

	/**
	 * Given order_id, find All info about the order.
	 */
	private void orderSearchbyID(int order_id) {

		try {
			/**
			 * Create the statement and sql
			 */
			Statement stm = conn.createStatement();

			String sql = "SELECT * FROM Orders WHERE order_id = " + order_id;

			System.out.println(sql);

			ResultSet rs = stm.executeQuery(sql);

			boolean exists = rs.next();
			if (!exists) {
				System.out.println("No such order");
				return;
			}

			String[] heads = { "order_id", "student_id", "order_date", "total_price", "💳 payment_method",
					"card_no", "order_delivered" };

			while (exists) {
				for (int i = 0; i < 7; i++) {
					String result = "";
					switch (heads[i]) {
						case "order_id":
						case "student_id":
							result = Integer.toString(rs.getInt(heads[i]));
							break;
						case "order_date":
							result = rs.getDate(heads[i]).toString();
							break;
						case "total_price":
							result = String.format("%.2f", rs.getBigDecimal(heads[i]));
							break;
						case "payment_method":
						case "order_delivered":
							result = rs.getString(heads[i]);
							break;
						case "card_no":
							if (rs.getString(heads[i]) == null) {
								result = "N/A";
							} else {
								result = rs.getString(heads[i]);
							}
							break;
					}
					System.out.println("➖" + heads[i] + " : " + result);
				}
				exists = rs.next();
			}

			rs.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			// noException = false;
		}
	}

	/*
	 * Given student_id, find all orders for the student driver.
	 */
	private void orderSearchforStudent(String choice) {
		int student_id = askForStudentId();

		if (student_id == -1) {
			System.out.println("No valid student ID was entered. Exiting the order search process.");
			return;
		}
		orderSearchbyStudentID(student_id, choice);

	}

	/*
	 * Given student_id, find all orders for the student.
	 */
	private void orderSearchbyStudentID(int student_id, String choice) {
		try {
			Statement stm = conn.createStatement();
			String sql = "SELECT order_id FROM Orders WHERE Student_id =" + student_id;
			ResultSet rs = stm.executeQuery(sql);

			boolean exists = rs.next();
			if (!exists) {
				System.out.println("No such order");
				return;
			}

			System.out.println("Lets us " + choice + " the order(s) now.");

			while (exists) { // this is the result record iterator, see the

				switch (choice) {
					case "Search":
						orderSearchbyID(rs.getInt(1));
						break;
					case "Update":
						updateOrder(rs.getInt(1));
						break;
					default:
						break;
				}
				// orderSearchbyID(rs.getInt(1));
				System.out.println("============================================");
				exists = rs.next();

			}
			rs.close();
			stm.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
			// noException = false;
		}
	}

	/*
	 * Given student_id, find all orders for the student that are not delivered.
	 */
	public boolean outstandingOrderSearchbyStudentID(int student_id) {
		try {
			Statement stm = conn.createStatement();
			String sql = "SELECT order_id FROM Orders WHERE student_id = " + student_id
					+ " AND order_delivered = 'pending'";

			ResultSet rs = stm.executeQuery(sql);

			boolean exists = rs.next();
			if (!exists) {
				System.out.println("No such order");
				return false;
			}

			while (exists) { // this is the result record iterator, see the

				orderSearchbyID(rs.getInt(1));
				System.out.println("============================================");
				exists = rs.next();

			}
			rs.close();
			stm.close();

			return true;
		} catch (SQLException e1) {
			e1.printStackTrace();
			// noException = false;
			return false;

		}
	}

	/**
	 * List all Orders in the database.
	 */
	private void listAllOrders() {
		System.out.println("All orders in the database now:");
		try {
			Statement stm = conn.createStatement();
			String sql = "SELECT order_id FROM Orders";
			System.out.println(sql);

			ResultSet rs = stm.executeQuery(sql);

			while (rs.next()) { // this is the result record iterator, see the

				orderSearchbyID(rs.getInt(1));
				System.out.println("============================================");

			}
			rs.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			// noException = false;
		}
	}

	/**
	 * Show all Books in the database.
	 */
	private void displayBooks() {

		try {
			/**
			 * Create the statement and sql
			 */
			Statement stm = conn.createStatement();

			String sql = "SELECT book_id FROM Book";

			System.out.println(sql);

			ResultSet rs = stm.executeQuery(sql);

			while (rs.next()) { // this is the result record iterator, see the

				diplayBook(rs.getInt(1));
				System.out.println("📚============================================📚");
				//
			}
			rs.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			// noException = false;
		}
	}

	/*
	 * Given book_id, display all info about the book.
	 */
	public void diplayBook(int book_id) {
		try {
			Statement stm = conn.createStatement();

			String sql = "SELECT * FROM Book WHERE book_id = " + book_id;

			ResultSet rs = stm.executeQuery(sql);

			while (rs.next()) { // this is the result record iterator, see the

				String[] heads = { "book_id", "title", "author", "price", "amount" };

				for (int i = 0; i < 5; ++i) {
					try {
						System.out.println(heads[i] + " : " + rs.getString(i + 1));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			rs.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			// noException = false;

		}
	}

	/**
	 * Given order_id, get total price.
	 */

	public double getTotalById(int order_id) {

		double result = 0;

		try {

			Statement stm = conn.createStatement();
			String sql = "SELECT total_price FROM Orders WHERE order_id = " + order_id;
			ResultSet rs = stm.executeQuery(sql);
			if (!rs.next())
				return 0;
			String[] heads = { " 🧳 total_price" };
			for (int i = 0; i < 1; ++i) {
				try {
					result = rs.getDouble(i + 1);
					System.out.println(heads[i] + " : " + result);
					// System.out.print(result);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			// noException = false;
		}

		return result;

	}

	/**
	 * Given student_id, update discount.
	 */

	public void updateDiscount(int student_id) {

		try {
			Statement stm = conn.createStatement();

			String sql = "UPDATE Student\n" +
					"SET discount = (\n" +
					"  CASE\n" +
					"    WHEN (\n" +
					"      SELECT SUM(total_price)\n" +
					"      FROM Orders\n" +
					"      WHERE student_id = " + student_id + "\n" +
					"      AND EXTRACT(YEAR FROM order_date) = EXTRACT(YEAR FROM SYSDATE)\n" +
					"    ) > 2000 THEN 0.20\n" +
					"    WHEN (\n" +
					"      SELECT SUM(total_price)\n" +
					"      FROM Orders\n" +
					"      WHERE student_id = " + student_id + "\n" +
					"      AND EXTRACT(YEAR FROM order_date) = EXTRACT(YEAR FROM SYSDATE)\n" +
					"    ) > 1000 THEN 0.10\n" +
					"    ELSE 0\n" +
					"  END\n" +
					")\n" +
					"WHERE student_id = " + student_id;

			// System.out.println(sql);

			stm.executeUpdate(sql);

			stm.close();

			System.out.println("succeed to update discount for student " + student_id);

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Could not update discount for student " + student_id);
			// noException = false;
		}
	}

	////////////////////////// Helper Functions //////////////////////////
	/*
	 * Ask to enter a student ID.
	 */
	public int askForStudentId() {
		while (true) {
			// prompt the user for a student ID
			System.out.println("Please enter a student ID:");
			String line = in.nextLine();
			int student_id = Integer.parseInt(line);

			// check if the student ID exists in the database
			if (checkStudentId(student_id)) {
				System.out.println("Student ID " + student_id + " exists in the database.");
				return student_id; // exit the method and return the valid student ID
			} else {
				System.out.println("Student ID " + student_id + " does not exist in the database.");

				// prompt the user to enter a new student ID or exit the program
				System.out.println("Would you like to enter a new student ID? (Y/N)");
				line = in.nextLine();

				if (line.equalsIgnoreCase("N")) {
					// exit the method without returning a valid student ID
					return -1; // or any other invalid value to indicate no valid student ID was entered
				}
			}
		}

	}

	/*
	 * Ask to enter a student ID.
	 */
	public int askForOrderId() {
		while (true) {
			// prompt the user for a student ID
			System.out.println("Please enter a Order ID:");
			String line = in.nextLine();
			int order_id = Integer.parseInt(line);

			// check if the student ID exists in the database
			if (checkOrder(order_id)) {
				System.out.println("Order ID " + order_id + " exists in the database.");
				return order_id; // exit the method and return the valid student ID
			} else {
				System.out.println("Order ID " + order_id + " does not exist in the database.");

				// prompt the user to enter a new student ID or exit the program
				System.out.println("Would you like to enter a new Order ID? (Y/N)");
				line = in.nextLine();

				if (line.equalsIgnoreCase("N")) {
					// exit the method without returning a valid student ID
					return -1; // or any other invalid value to indicate no valid student ID was entered
				}
			}
		}

	}

	/*
	 * Check if the student ID exists in the database.
	 */
	public boolean checkStudentId(int student_id) {
		try {
			Statement stm = conn.createStatement();

			String sql = "SELECT * FROM Student WHERE Student_id = " + student_id;

			System.out.println(sql);

			ResultSet result = stm.executeQuery(sql);

			boolean exists = result.next();

			if (!exists) {
				System.out.println("No such Student ID exists in the database.");
			}

			stm.close();
			result.close();

			return exists;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean allDelivered(int order_id) {
		try {
			Statement stm = conn.createStatement();

			String sql = "SELECT * FROM Orders_Book WHERE delivery_date > SYSDATE AND order_id = " + order_id;

			System.out.println(sql);

			ResultSet result = stm.executeQuery(sql);

			boolean exists = result.next();

			if (exists) {
				System.out.println("There are still pending orders.");
			}

			stm.close();
			result.close();

			return !exists;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean allNOTDelivered(int order_id) {
		try {
			Statement stm = conn.createStatement();

			String sql = "SELECT * FROM Orders_Book WHERE delivery_date <= SYSDATE AND order_id = " + order_id;

			System.out.println(sql);

			ResultSet result = stm.executeQuery(sql);

			boolean exists = result.next();

			if (exists) {
				System.out.println("There are delivered orders...");
			}

			stm.close();
			result.close();

			return !exists;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void updateOrder(int order_id) {
		try {
			Statement stm = conn.createStatement();

			if (allDelivered(order_id)) {
				System.out.println("All books in order " + order_id + " have been delivered.");
			} else {
				// System.out.println("There are still pending orders.");
				return;
			}

			String sql = "UPDATE Orders SET order_delivered = 'delivered' WHERE order_id = " + order_id;

			System.out.println(sql);

			stm.executeUpdate(sql);

			stm.close();

			System.out.println("succeed to update order " + order_id);

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Could not update order " + order_id);
			// noException = false;
		}
	}

	public void update() {

		System.out.println("All orders in the database now:");
		try {
			Statement stm = conn.createStatement();
			String sql = "SELECT order_id FROM Orders";
			System.out.println(sql);

			ResultSet rs = stm.executeQuery(sql);

			while (rs.next()) { // this is the result record iterator, see the

				updateOrder(rs.getInt(1));
				System.out.println("============================================");

			}
			rs.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			// noException = false;
		}

	}

	/**
	 * Insert data into database
	 * 
	 * @return
	 */
	private void placeOrder() {
		/**
		 * A sample input is:
		 */
		int student_id = askForStudentId();

		if (student_id == -1) {
			System.out.println("No valid student ID was entered. Exiting the order placement process.");
			return;
		}

		// check if have outstanding order
		if (outstandingOrderSearchbyStudentID(student_id)) {
			System.out.println("You have outstanding order. Please wait.");
			return;
		} else {
			System.out.println("You don't have outstanding order. You can place order now.");
		}

		// int student_id = Integer.parseInt(line);

		int order_id = 0;
		Random rand = new Random();

		do {
			order_id = rand.nextInt(9001) + 1000; // Generate a random order ID between 1000 and 10000
		} while (checkOrder(order_id)); // Keep looping while the order ID is already taken

		System.out.println("Order ID: " + order_id);

		System.out.println("👋 Welcome to our bookshop! Here are our Books: ");

		displayBooks();

		// Prompt the user to enter the number of different books to order
		System.out.print("How many different books would you like to order? ");
		int numBooks = in.nextInt();
		in.nextLine(); // consume the remaining newline character

		double total_price = 0;

		// Create an ArrayList to record successful book orders
		ArrayList<BookOrder> orders = new ArrayList<>();
		HashSet<Integer> addedBookIds = new HashSet<>();

		// Loop through the number of different books and prompt the user to enter the
		// book ID and amount for each book
		for (int i = 1; i <= numBooks; i++) {
			System.out.println("Enter information for book " + i + "....");

			int bookId = askBookId();

			if (bookId == -1) {
				System.out.println("No valid book ID was entered. Exiting the order placement process.");
				return;
			}

			// Check if the book ID is already in the order
			if (addedBookIds.contains(bookId)) {
				System.out.println("This book is already in your order. Do you want to change the amount? (y/n)");

				String answer = in.nextLine();
				if (answer.equalsIgnoreCase("y")) {
					int oldAmount = 0;
					for (int j = 0; i < orders.size(); i++) {
						if (orders.get(i).getBookId() == bookId) {
							oldAmount = orders.get(i).getBookAmount();
							break;
						}
					}

					System.out.print("New book amount: ");
					int newAmount = in.nextInt();
					in.nextLine(); // consume the remaining newline character

					// Check if there is enough stock of the book
					if (newAmount > getAmount(bookId)) {
						System.out.println("We don't have enough stock of this book. Sorry!");
						continue;
					} else {
						// Update the amount of the book in the order
						for (BookOrder order : orders) {
							if (order.getBookId() == bookId) {
								order.bookAmount = newAmount;
								break;
							}
						}

						// Get the price of the book by ID from the database
						double book_price = getPriceByID(bookId);

						// Calculate the total price difference for this book
						double book_total_price_diff = (newAmount * book_price) - (oldAmount * book_price);

						// Add the total price difference for this book to the overall total price
						total_price += book_total_price_diff;

					}
				} else {
					continue;
				}
			} else {

				// Prompt the user to enter the book amount
				System.out.print("Book amount: ");
				int bookAmount = in.nextInt();
				in.nextLine(); // consume the remaining newline character

				// Check if there is enough stock of the book
				int stock = getAmount(bookId);
				if (bookAmount > stock) {
					System.out.println("We don't have enough stock of this book. Sorry! We only have " + stock);
					continue;
				} else {
					// Record the successful book order
					BookOrder order = new BookOrder(bookId, bookAmount);
					orders.add(order);
					addedBookIds.add(bookId); // add the book ID to the set of added book IDs

					// Get the price of the book by ID from the database
					double book_price = getPriceByID(bookId);

					// Calculate the total price for this book
					double book_total_price = bookAmount * book_price;

					// Add the total price for this book to the overall total price
					total_price += book_total_price;

				}
			}
		}

		if (orders.size() > 0) {

			double discount = getDiscount(student_id);
			total_price = total_price * (1 - discount);

			System.out.println("Your total is... " + total_price);
			// Ask if the user wants to pay
			System.out.println("Do you want to proceed with payment? (Y/N)");
			String answer = in.nextLine();

			// Check the answer
			if (answer.equalsIgnoreCase("Y")) {
				boolean paymentSuccess = false;
				while (!paymentSuccess) {
					String[] payment_result = getPaymentInfo();
					String payment_method = payment_result[0];
					String card_no = payment_result[1];

					try {
						String insertResult = insertOrder(order_id, student_id, total_price, payment_method, card_no);
						if (insertResult.equals("error")) {
							System.out.println("Invalid card number. Do you want to try again? (Y/N)");
							Scanner scanner = new Scanner(System.in);
							String response = scanner.nextLine();
							if (!response.equalsIgnoreCase("Y")) {
								return;
							}
						} else {
							System.out.println("Payment successful!");
							paymentSuccess = true;
							for (BookOrder order : orders) {
								try {
									InsertBook(order_id, order.getBookId(), order.getBookAmount());
								} catch (SQLException e) {
									System.out.println("Error inserting book order details: " + e.getMessage());
								}
							}
						}

					} catch (SQLException e) {
						System.out.println("Error inserting order details: " + e.getMessage());
					}
				}

			} else {

				System.out.println("Payment cancelled. Order not placed.");
			}

			updateDiscount(student_id);
		} else {
			System.out.println("No books were added to the order. Exiting the order placement process.");
		}
		// return;

	}

	public void InsertBook(int order_id, int bookId, int bookAmount) throws SQLException {

		try {
			Statement stm = conn.createStatement();

			String sql = "INSERT INTO Orders_Book VALUES (" + order_id + "," + bookId + "," + bookAmount + ","
					+ "SYSDATE + INTERVAL '14' DAY)";

			System.out.println(sql);

			stm.executeUpdate(sql); // please pay attention that we use
									// executeUpdate to update the database

			stm.close();

			System.out.println("succeed to insert Book" + bookId);

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("fail to Insert Book " + bookId);
			// noException = false;
			// return "error";
		}
	}

	public String insertOrder(int order_id, int student_id, double total_price, String payment_method, String card_no)
			throws SQLException {

		try {
			Statement stm = conn.createStatement();

			String sql = "INSERT INTO Orders (order_id, student_id, order_date, total_price, payment_method, card_no) "
					+
					"VALUES (" + order_id + ", " + student_id + ", SYSDATE, " + total_price + ", '" + payment_method
					+ "', '" + card_no + "')";

			System.out.println(sql);

			stm.executeUpdate(sql); // please pay attention that we use
									// executeUpdate to update the database

			stm.close();

			System.out.println("succeed to insert Order " + order_id);
			return "success";

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("fail to insert order " + order_id);
			// noException = false;
			return "error";
		}
	}

	public String cancelOrder(int order_id) {

		try {
			Statement stm = conn.createStatement();

			String sql = "DELETE FROM Orders WHERE order_id = " + order_id;

			System.out.println(sql);

			stm.executeUpdate(sql); // please pay attention that we use
									// executeUpdate to update the database

			stm.close();

			System.out.println("succeed to delete Order " + order_id);
			return "success";

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("fail to delete order " + order_id);
			// noException = false;
			return "error";
		}
	}

	public void cancelOrder() {

		/**
		 * A sample input is:
		 */
		int order_id = askForOrderId();

		if (order_id == -1) {
			System.out.println("No valid order ID was entered. Exiting the order cancel process.");
			return;
		}

		System.out.println("Information about your order...");

		orderSearchbyID(order_id);

		if (!allNOTDelivered(order_id)) {
			System.out.println("Some or All books in this order have been delivered. You cannot cancel this order.");
			return;
		} else {
			System.out.println("All books in this order have not been delivered.");
		}

		if (getOrderAgeInDays(order_id) > 7) {
			System.out.println("This order is older than 7 days. You cannot cancel this order.");
			return;
		}

		System.out.println("Do you want to cancel this order? (Y/N)");

		String answer = in.nextLine();

		if (answer.equalsIgnoreCase("Y")) {
			if (cancelOrder(order_id).equals("error")) {
				System.out.println("Error cancelling order. Start Over...");
				return;
			} else {
				System.out.println("Order cancelled successfully!");
			}
		} else {
			System.out.println("You have chosen not to cancel the order.");
		}

	}

	/*
	 * Check if the order ID exists in the database.
	 */
	public boolean checkOrder(int order_id) {
		try {
			Statement stm = conn.createStatement();

			String sql = "SELECT * FROM Orders WHERE order_id = " + order_id;

			System.out.println(sql);

			ResultSet result = stm.executeQuery(sql);

			boolean exists = result.next();

			if (!exists) {
				System.out.println("No such Order ID exists in the database.");
			}

			stm.close();
			result.close();

			return exists;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public int getOrderAgeInDays(int order_id) {
		try {
			Statement stm = conn.createStatement();

			String sql = "SELECT order_date FROM Orders WHERE order_id = " + order_id;

			System.out.println(sql);

			ResultSet result = stm.executeQuery(sql);

			boolean exists = result.next();

			if (!exists) {
				System.out.println("This order does not exist.");
				return -1; // return -1 to indicate that the order does not exist
			}

			// get the order date from the result set
			Date orderDate = result.getDate("order_date");

			// calculate the age of the order in days
			long ageInMillis = System.currentTimeMillis() - orderDate.getTime();
			int ageInDays = (int) TimeUnit.MILLISECONDS.toDays(ageInMillis);

			stm.close();
			result.close();

			return ageInDays;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1; // return -1 to indicate an error occurred
		}
	}

	/*
	 * Check if the book ID exists in the database.
	 */
	public boolean checkBook(int book_id) {
		try {
			Statement stm = conn.createStatement();

			String sql = "SELECT * FROM Book WHERE book_id = " + book_id;

			System.out.println(sql);

			ResultSet result = stm.executeQuery(sql);

			boolean exists = result.next();

			stm.close();
			result.close();

			return exists;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * Ask to enter a book ID.
	 */
	public int askBookId() {
		int book_id = -1;
		boolean valid_id = false;

		while (!valid_id) {
			System.out.print("Enter book ID (or -1 to exit): ");
			book_id = in.nextInt();
			in.nextLine(); // consume the remaining newline character

			if (book_id == -1) {
				return -1; // Return -1 to indicate program exit
			} else if (checkBook(book_id)) {
				valid_id = true;
			} else {
				System.out.println("Invalid book ID.");
			}
		}

		return book_id;
	}

	/*
	 * Ask to enter a book amount.
	 */
	int getAmount(int book_id) {

		int result = 0;

		try {

			Statement stm = conn.createStatement();
			String sql = "SELECT amount FROM Book WHERE book_id = " + book_id;

			ResultSet rs = stm.executeQuery(sql);
			if (!rs.next())
				return 0;
			String[] heads = { "📖 amount" };
			for (int i = 0; i < 1; ++i) {
				try {
					result = rs.getInt(i + 1);
					System.out.println(heads[i] + " : ");
					System.out.println(result);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			// noException = false;
		}

		return result;
	}

	/*
	 * Get the discount of a student.
	 */
	double getDiscount(int student_id) {

		double result = 0;

		try {

			Statement stm = conn.createStatement();
			String sql = "SELECT discount FROM Student WHERE student_id = " + student_id;
			ResultSet rs = stm.executeQuery(sql);
			if (!rs.next())
				return 0;
			String[] heads = { "💸💸💸discount" };
			for (int i = 0; i < 1; ++i) {
				try {
					result = rs.getDouble(i + 1);
					System.out.println(heads[i] + " : ");
					System.out.println(result);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			// noException = false;
		}

		return result;
	}

	/*
	 * Get the price of a book.
	 */
	double getPriceByID(int book_id) {

		double result = 0;

		try {

			Statement stm = conn.createStatement();
			String sql = "SELECT price FROM Book WHERE book_id = " + book_id;
			ResultSet rs = stm.executeQuery(sql);
			if (!rs.next())
				return 0;
			String[] heads = { "📖 book_price" };
			for (int i = 0; i < 1; ++i) {
				try {
					result = rs.getDouble(i + 1);
					System.out.println(heads[i] + " : ");
					System.out.println(result);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			// noException = false;
		}

		return result;

	}

	/*
	 * Get the payment information.
	 */
	private String[] getPaymentInfo() {
		String[] result = new String[2];
		// Define payment options
		String[] paymentOptions = { "Apple Pay", "AliPay", "Credit Card" };

		// Print payment options
		System.out.println("Please choose a payment method:");
		for (int i = 0; i < paymentOptions.length; i++) {
			System.out.println((i + 1) + ". " + paymentOptions[i]);
		}

		// Read user input for payment method
		int paymentOption = in.nextInt();
		String paymentMethod;

		// Assign payment method based on user input
		switch (paymentOption) {
			case 1:
				paymentMethod = "Apple Pay";
				break;
			case 2:
				paymentMethod = "AliPay";
				break;
			case 3:
				paymentMethod = "Credit Card";

				// !! If payment method is credit card, prompt for card number
				break;
			default:
				System.out.println("Invalid payment option selected.");
				System.out.println("Do you want to select a valid payment option? (Y/N)");
				String answer = in.next();
				if (answer.equalsIgnoreCase("Y")) {
					return getPaymentInfo(); // recursively call the method to get a valid payment option
				} else {
					return null; // user chose not to enter a valid payment option
				}
		}

		String cardNumber = null;

		if (paymentMethod.equalsIgnoreCase("Credit Card")) {
			// If payment method is credit card, prompt for card number
			cardNumber = getCreditCard();
			// Process credit card payment with the card number
			System.out.println("Processing credit card payment with card number: " + cardNumber);
		} else {
			// Process non-credit card payment
			System.out.println("Processing " + paymentMethod + " payment.");
		}

		result[0] = paymentMethod;
		result[1] = cardNumber;

		return result; // return the chosen payment method
	}

	private String getCreditCard() {
		System.out.print("Enter card number: ");
		String cardNumber = in.next();
		System.out.println("Processing credit card payment with card number: " + cardNumber);
		return cardNumber;
	}

	/**
	 * Please fill in this function to delete a flight.
	 */
	// public void deleteFlight() {
	// listAllFlights();
	// System.out.println("Please input the flight_no to delete:");
	// String line = in.nextLine();

	// if (line.equalsIgnoreCase("exit"))
	// return;
	// line = line.trim();

	// try {
	// Statement stm = conn.createStatement();

	// String sql = "Delete from FLIGHTS " + "Where Flight_no = '" + line + "'";

	// System.out.println(sql);

	// /*
	// * Formuate your own SQL query:
	// *
	// * sql = "...";
	// *
	// */

	// stm.executeUpdate(sql); // please pay attention that we use
	// // executeUpdate to update the database

	// stm.close();

	// /*
	// * You may uncomment the statement below after formulating the SQL
	// * query above
	// *
	// * System.out.println("succeed to delete flight " + line);
	// *
	// */
	// } catch (SQLException e) {
	// e.printStackTrace();
	// System.out.println("fail to delete flight " + line);
	// noException = false;
	// }
	// }

	/**
	 * Close the manager. Do not change this function.
	 */
	public void close() {
		System.out.println("Thanks for using this manager! Bye...");
		try {
			if (conn != null)
				conn.close();
			if (proxySession != null) {
				proxySession.disconnect();
			}
			in.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor of flight manager Do not change this function.
	 */
	public UniversityBookshop() {
		System.out.println("Welcome to use this manager!");
		in = new Scanner(System.in);
	}

	/**
	 * Main function
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		UniversityBookshop manager = new UniversityBookshop();
		if (!manager.loginProxy()) {
			System.out.println("Login proxy failed, please re-examine your username and password!");
			return;
		}
		if (!manager.loginDB()) {
			System.out.println("Login database failed, please re-examine your username and password!");
			return;
		}
		System.out.println("Login succeed!");
		try {
			manager.run();
		} finally {
			manager.close();
		}
	}
}
