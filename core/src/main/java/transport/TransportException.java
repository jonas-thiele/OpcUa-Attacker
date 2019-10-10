package transport;

import opcua.message.ErrorMessage;

public class TransportException extends Exception {
    private ErrorMessage errorMessage;

    public TransportException() {
        super();
    }

    public TransportException(String message) {
        super(message);
    }

    public TransportException(ErrorMessage errorMessage) {
        super();
        this.errorMessage = errorMessage;
    }

    public TransportException(String message, ErrorMessage errorMessage) {
        super(message);
        this.errorMessage = errorMessage;
    }

    public TransportException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransportException(Throwable cause) {
        super(cause);
    }

    protected TransportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String toString() {
        String str = super.toString();
        if(errorMessage != null) {
            str = str + "\nError message:\n" + errorMessage;
        }
        return str;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }
}
