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

public class FlightManager {

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

	String[] options = { // if you want to add an option, append to the end of
							// this array
			"order search", "order place", "order cancel",
			"exit" };

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

	/**
	 * Login the proxy. Do not change this function.
	 * 
	 * @return boolean
	 */
	public boolean loginProxy() {
		if (getYESorNO("Using ssh tunnel or not?")) { // if using ssh tunnel
			String[] namePwd = getUsernamePassword("Login cs lab computer");
			String sshUser = namePwd[0];
			String sshPwd = namePwd[1];
			try 
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
		}else

	{
		jdbcHost = databaseHost;
		jdbcPort = databasePort;
	}return true;
	}

	/**
	 * Login the oracle system. Change this function under instruction.
	 * 
	 * @return boolean
	 */
	public boolean loginDB() {
		String username = "f1204276";// Replace e1234567 to your username
		String password = "f1204276";// Replace e1234567 to your password

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
				// ordersearch()
				// addFlight();
			} else if (choice == 2) {
				placeorder();
			} else if (choice == 3) {
				// cancelorder()
				printFlightByNo();
				// } else if (options[choice - 1].equals("select a flight (by source, dest,
				// stop_no = 0)")) {
				// selectFlightsInZeroStop();
				// } else if (options[choice - 1].equals("select a flight (by source, dest,
				// stop_no = 1)")) {
				// selectFlightsInOneStop();
			} else if (options[choice - 1].equals("exit")) {
				break;
			}
		}
	}

	private void orderSearch(String student_id) {
		try {
			Statement stm = conn.createStatement();
			String sql = "SELECT * FROM Orders WHERE Student_id = '" + student_id + "'";
			ResultSet rs = stm.executeQuery(sql);
			if (!rs.next())
				return;
			String[] heads = { "Flight_no", "Depart_Time", "Arrive_Time", "Fare", "Source", "Dest" };
			for (int i = 0; i < 6; ++i) { // flight table 6 attributes
				try {
					System.out.println(heads[i] + " : " + rs.getString(i + 1)); // attribute
																				// id
																				// starts
																				// with
																				// 1
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			noException = false;
		}
	}

	/**
	 * List all flights in the database.
	 */
	private void listAllFlights() {
		System.out.println("All flights in the database now:");
		try {
			Statement stm = conn.createStatement();
			String sql = "SELECT Flight_no FROM FLIGHTS";
			ResultSet rs = stm.executeQuery(sql);

			int resultCount = 0;
			while (rs.next()) {
				System.out.println(rs.getString(1));
				++resultCount;
			}
			System.out.println("Total " + resultCount + " flight(s).");
			rs.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			noException = false;
		}
	}

	/**
	 * Select out a flight according to the flight_no.
	 */
	private void printFlightByNo() {
		listAllFlights();
		System.out.println("Please input the flight_no to print info:");
		String line = in.nextLine();
		line = line.trim();
		if (line.equalsIgnoreCase("exit"))
			return;

		// printFlightInfo(line);
	}

	/**
	 * Given source and dest, select all the flights can arrive the dest
	 * directly. For example, given HK, Tokyo, you may find HK -> Tokyo Your job
	 * to fill in this function.
	 */
	private void selectFlightsInZeroStop() {
		System.out.println("Please input source, dest:");

		String line = in.nextLine();

		if (line.equalsIgnoreCase("exit"))
			return;

		String[] values = line.split(",");
		for (int i = 0; i < values.length; ++i)
			values[i] = values[i].trim();

		try {
			/**
			 * Create the statement and sql
			 */
			Statement stm = conn.createStatement();

			String sql = "select Flight_no from Flights where Source = '" + values[0] + "' AND Dest = '" + values[1]
					+ "'";

			/**
			 * Formulate your own SQL query:
			 *
			 * sql = "...";
			 *
			 */
			System.out.println(sql);
			//
			ResultSet rs = stm.executeQuery(sql);

			int resultCount = 0; // a counter to count the number of result
			// // records
			while (rs.next()) { // this is the result record iterator, see the
				// // tutorial for details
				//
				// /*
				// * Write your own to print flight information; you may use the
				// * printFlightInfo() function
				// */

				// printFlightInfo(rs.getString(1));
				++resultCount;
				System.out.println("============================================");
				//
			}
			System.out.println("Total " + resultCount + " choice(s).");
			rs.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			noException = false;
		}
	}

	public int getTotalById(int order_id) {

		int result = 0;

		try {

			Statement stm = conn.createStatement();
			String sql = "SELECT total_price FROM Orders WHERE order_id = " + order_id + ";";
			ResultSet rs = stm.executeQuery(sql);
			if (!rs.next())
				return 0;
			String[] heads = { "total_price" };
			for (int i = 0; i < 1; ++i) {
				try {
					result = rs.getInt(i + 1);
					System.out.println(heads[i] + " : " + result);
					// System.out.print(result);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			noException = false;
		}

		return result;

	}

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
					"WHERE student_id = " + student_id + ";";

			System.out.println(sql);

			stm.executeUpdate(sql);

			stm.close();

			System.out.println("succeed to update discount for student " + student_id);

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Could not update discount for student " + student_id);
			noException = false;
		}
	}

	/**
	 * Insert data into database
	 * 
	 * @return
	 */
	private void placeorder() {
		/**
		 * A sample input is:
		 */
		System.out.println("Please input student-id or exit:");
		String line = in.nextLine();

		if (line.equalsIgnoreCase("exit"))
			return;

		int student_id = Integer.parseInt(line);

		System.out.println("Please input order-id:");
		int order_id = Integer.parseInt(in.nextLine());

		// Prompt the user to enter the number of different books to order
		System.out.print("How many different books would you like to order? ");
		int numBooks = in.nextInt();
		in.nextLine(); // consume the remaining newline character

		int counter = 0;
		double total_price = 0;

		// Create an ArrayList to record successful book orders
		ArrayList<BookOrder> orders = new ArrayList<>();
		HashSet<Integer> addedBookIds = new HashSet<>();

		// Loop through the number of different books and prompt the user to enter the
		// book ID and amount for each book
		for (int i = 1; i <= numBooks; i++) {
			System.out.println("Enter information for book " + i + "....");

			// Prompt the user to enter the book ID
			System.out.print("Book ID: ");

			// if already in order
			int bookId = in.nextInt();
			in.nextLine(); // consume the remaining newline character

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

						counter++;
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
				if (bookAmount > getAmount(bookId)) {
					System.out.println("We don't have enough stock of this book. Sorry!");
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

					counter++;

				}
			}
		}

		if (counter > 0) {
			// updateTotal(order_id, student_id);
			// total_price = getTotalById(order_id);

			double discount = getDiscount(student_id);
			total_price = total_price * (1 - discount);

			System.out.println("Your total is... " + total_price);
			// Ask if the user wants to pay
			System.out.println("Do you want to proceed with payment? (Y/N)");
			String answer = in.nextLine();

			// Check the answer
			if (answer.equalsIgnoreCase("Y")) {
				// Proceed with payment
				String[] payment_result = getPaymentInfo();
				String payment_method = payment_result[0];
				String card_no = payment_result[1];

				try {
					// Insert the order details into the Orders table
					insertOrder(order_id, student_id, total_price, payment_method);
					System.out.println("Success! I will add info for every book now...");

					// Loop through the book orders and insert the details into the Order_Details
					// table
					for (BookOrder order : orders) {
						InsertBook(order_id, order.getBookId(), order.getBookAmount());
					}

					System.out.println("Success! All order information has been added.");
				} catch (SQLException e) {
					System.out.println("Error: " + e.getMessage());
				}

			} else {
				// Cancel payment , cancel order
			}

			updateDiscount(student_id);
		}

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

			System.out.println("succeed to insert Order " + order_id);

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("fail to Insert Book " + bookId);
			noException = false;
			// return "error";
		}
	}

	public void insertOrder(int order_id, int student_id, double total_price, String payment_method)
			throws SQLException {

		try {
			Statement stm = conn.createStatement();

			String sql = "INSERT INTO Orders (order_id, student_id, order_date, total_price, payment_method) " +
					"VALUES (" + order_id + ", " + student_id + ", SYSDATE, " + total_price + ", '" + payment_method
					+ "')";
			System.out.println(sql);

			stm.executeUpdate(sql); // please pay attention that we use
									// executeUpdate to update the database

			stm.close();

			System.out.println("succeed to insert Order " + order_id);

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("fail to delete flight " + line);
			noException = false;
			// return "error";
		}
	}

	double getAmount(int book_id) {

		int result = 0;

		try {

			Statement stm = conn.createStatement();
			String sql = "SELECT amount FROM Book WHERE book_id = " + book_id + ";";

			ResultSet rs = stm.executeQuery(sql);
			if (!rs.next())
				return 0;
			String[] heads = { "book_price" };
			for (int i = 0; i < 1; ++i) {
				try {
					result = rs.getInt(i + 1);
					System.out.println(heads[i] + " : ");
					System.out.print(result);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			noException = false;
		}

		return result;
	}

	double getDiscount(int student_id) {

		double result = 0;

		try {

			Statement stm = conn.createStatement();
			String sql = "SELECT discount FROM Student WHERE student_id = " + student_id + ";";
			ResultSet rs = stm.executeQuery(sql);
			if (!rs.next())
				return 0;
			String[] heads = { "book_price" };
			for (int i = 0; i < 1; ++i) {
				try {
					result = rs.getInt(i + 1);
					System.out.println(heads[i] + " : ");
					System.out.print(result);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			noException = false;
		}

		return result;
	}

	double getPriceByID(int book_id) {

		double result = 0;

		try {

			Statement stm = conn.createStatement();
			String sql = "SELECT price FROM Book WHERE book_id = " + book_id + ";";
			ResultSet rs = stm.executeQuery(sql);
			if (!rs.next())
				return 0;
			String[] heads = { "book_price" };
			for (int i = 0; i < 1; ++i) {
				try {
					result = rs.getInt(i + 1);
					System.out.println(heads[i] + " : ");
					System.out.print(result);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			noException = false;
		}

		return result;

	}

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
			System.out.print("Enter card number: ");
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
	public void deleteFlight() {
		listAllFlights();
		System.out.println("Please input the flight_no to delete:");
		String line = in.nextLine();

		if (line.equalsIgnoreCase("exit"))
			return;
		line = line.trim();

		try {
			Statement stm = conn.createStatement();

			String sql = "Delete from FLIGHTS " + "Where Flight_no = '" + line + "'";

			System.out.println(sql);

			/*
			 * Formuate your own SQL query:
			 *
			 * sql = "...";
			 *
			 */

			stm.executeUpdate(sql); // please pay attention that we use
									// executeUpdate to update the database

			stm.close();

			/*
			 * You may uncomment the statement below after formulating the SQL
			 * query above
			 *
			 * System.out.println("succeed to delete flight " + line);
			 *
			 */
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("fail to delete flight " + line);
			noException = false;
		}
	}

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
	public FlightManager() {
		System.out.println("Welcome to use this manager!");
		in = new Scanner(System.in);
	}

	/**
	 * Main function
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		FlightManager manager = new FlightManager();
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
