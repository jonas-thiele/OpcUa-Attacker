package opcua.encoding;

public class EncodingException extends Exception {
    public EncodingException() {
        super();
    }

    public EncodingException(String message) {
        super(message);
    }

    public EncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncodingException(Throwable cause) {
        super(cause);
    }

    protected EncodingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
