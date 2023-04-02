public class BookOrder {
    private int bookId;
    private int bookAmount;

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
}