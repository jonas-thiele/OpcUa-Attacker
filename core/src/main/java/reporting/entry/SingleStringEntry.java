package reporting.entry;

public class SingleStringEntry extends LeafEntry {

    private final String message;

    public SingleStringEntry(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message + "\n";
    }
}
