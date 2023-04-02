import java.util.*;

public class BookOrder {
    public int bookId;
    public int bookAmount;

    public BookOrder(int bookId, int bookAmount) {
        this.bookId = bookId;
        this.bookAmount = bookAmount;
    }

    public int getBookId() {
        return bookId;
    }

    public int getBookAmount() {
        return bookAmount;
    }

    // New method to get the amount of a book order based on its ID
    public static int getBAmount(ArrayList<BookOrder> orders, int bookId) {
        int amount = 0;
        for (BookOrder order : orders) {
            if (order.getBookId() == bookId) {
                amount = order.getBookAmount();
                break;
            }
        }
        return amount;
    }
}
