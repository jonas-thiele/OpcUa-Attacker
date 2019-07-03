package attacks.manger.oracle;

public class OracleException extends Exception {
    public OracleException(String message) {
        super(message);
    }

    public OracleException(String message, Throwable cause) {
        super(message, cause);
    }

    public OracleException(Throwable cause) {
        super(cause);
    }
}
