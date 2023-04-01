import java.awt.GridLayout;
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
	 * Login the oracle system. Change this function under instruction.
	 * 
	 * @return boolean
	 */
	public boolean loginDB() {
		String username = "f1204276";//Replace e1234567 to your username
		String password = "f1204276";//Replace e1234567 to your password
		
		/* Do not change the code below */
		if(username.equalsIgnoreCase("e1234567") || password.equalsIgnoreCase("e1234567")) {
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
//				addFlight();
			} else if (choice == 2) {
				placeorder();
			} else if (choice == 3) {
				// cancelorder()
				printFlightByNo();
//			} else if (options[choice - 1].equals("select a flight (by source, dest, stop_no = 0)")) {
//				selectFlightsInZeroStop();
//			} else if (options[choice - 1].equals("select a flight (by source, dest, stop_no = 1)")) {
//				selectFlightsInOneStop();
			} else if (options[choice - 1].equals("exit")) {
				break;
			}
		}
	}

	/**
	 * Print out the infomation of a flight given a flight_no
	 * 
	 * @param student_id
	 */
//	private void printFlightInfo(String flight_no) {
//		try {
//			Statement stm = conn.createStatement();
//			String sql = "SELECT * FROM FLIGHTS WHERE Flight_no = '" + flight_no + "'";
//			ResultSet rs = stm.executeQuery(sql);
//			if (!rs.next())
//				return;
//			String[] heads = { "Flight_no", "Depart_Time", "Arrive_Time", "Fare", "Source", "Dest" };
//			for (int i = 0; i < 6; ++i) { // flight table 6 attributes
//				try {
//					System.out.println(heads[i] + " : " + rs.getString(i + 1)); // attribute
//																				// id
//																				// starts
//																				// with
//																				// 1
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
//			}
//		} catch (SQLException e1) {
//			e1.printStackTrace();
//			noException = false;
//		}
//	}
	
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

//		printFlightInfo(line);
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

			String sql = "select Flight_no from Flights where Source = '" + values[0] + "' AND Dest = '" + values[1] + "'";
			

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
//									// records
			while (rs.next()) { // this is the result record iterator, see the
//								// tutorial for details
//
//				/*
//				 * Write your own to print flight information; you may use the
//				 * printFlightInfo() function
//				 */
				
//				printFlightInfo(rs.getString(1));
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

	/**
	 * Given source and dest, select all the flights can arrive the dest in one
	 * stop. For example, given HK, Tokyo, you may find HK -> Beijing, Beijing
	 * -> Tokyo Your job to fill in this function.
	 */
	private void selectFlightsInOneStop() {
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

//			String sql = "select Flight_no from Flights where Source = '" + values[0] + "' AND Dest = '" + values[1] + "'";
			
			String sql = "select F1.Flight_no, F2.Flight_no from Flights F1, Flights F2 where F1.Source = '" + values[0] + "' AND F1.Dest = F2.Source AND F2.Dest = '" + values[1] + "' and F1.Arrive_time <= F2.Depart_time";
			

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
//									// records
			while (rs.next()) { // this is the result record iterator, see the
//								// tutorial for details
//
//				/*
//				 * Write your own to print flight information; you may use the
//				 * printFlightInfo() function
//				 */
				
//				printFlightInfo(rs.getString(1));
				System.out.println("--------------------------------------------");
//				printFlightInfo(rs.getString(2));
				++resultCount;
//				System.out.println("============================================");
//
			}
			System.out.println("Total " + resultCount + " choice(s).");
			rs.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			noException = false;
		}
		
		

		/**
		 * try {
		 * 
		 * // Similar to the 'selectFlightsInZeroStop' function; write your own
		 * code here
		 * 
		 * 
		 * } catch (SQLException e) { e.printStackTrace(); noException = false;
		 * }
		 */
	}

	/**
	 * Insert data into database
	 * 
	 * @return
	 */
	private void placeorder() {
		/**
		 * A sample input is: CX109, 2015/03/15/13:00:00, 2015/03/15/19:00:00,
		 * 2000, Beijing, Tokyo
		 */
		System.out.println("Please input student-id or exit:");
		String student_id = in.nextLine();
		
		if (student_id.equalsIgnoreCase("exit"))
			return;
		
		System.out.println("Please input order-id:");
		String order_id = in.nextLine();
		
		// Prompt the user to enter the number of different books to order
        System.out.print("How many different books would you like to order? ");
        int numBooks = in.nextInt();
        in.nextLine(); // consume the remaining newline character
        
        try {
        // Loop through the number of different books and prompt the user to enter the book ID and amount for each book
        for (int i = 1; i <= numBooks; i++) {
            System.out.println("Enter information for book " + i + ":");
            
            // Prompt the user to enter the book ID
            System.out.print("Book ID: ");
            int bookId = in.nextInt();
            in.nextLine(); // consume the remaining newline character
            
            // Prompt the user to enter the book amount
            System.out.print("Book amount: ");
            int bookAmount = in.nextInt();
            in.nextLine(); // consume the remaining newline character
            
            // Print out the entered values
            System.out.println("Book " + i + " - ID: " + bookId + ", Amount: " + bookAmount);
       
		
			Statement stm = conn.createStatement();
	
			
			String sql = "INSERT INTO Orders_Book VALUES (" + order_id + "," + bookId + "," + bookAmount + "," + "SYSDATE)";
           
			
			stm.executeUpdate(sql);
			stm.close();
			System.out.println("succeed to add flight ");
//			printFlightInfo(values[0]);
        }
        } catch (SQLException e) {
			e.printStackTrace();
//			System.out.println("fail to add a flight " + line);
			noException = false;
		}
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

			String sql = "Delete from FLIGHTS " + "Where Flight_no = '" + line + "'" ;
			
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
