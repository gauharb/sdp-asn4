package exception;

public class OrderException extends Exception {
    public OrderException(String message, Throwable cause) { super(message, cause); }
    public OrderException(String message) { super(message); }
}