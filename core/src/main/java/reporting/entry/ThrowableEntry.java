package reporting.entry;

public class ThrowableEntry extends LeafEntry {
    private final Throwable e;

    public ThrowableEntry(Throwable e) {
        this.e = e;
    }

    public Throwable getThrowable() {
        return e;
    }

    @Override
    public String toString() {
        return e.getClass().getName() + ": " + e.getMessage();
    }
}
