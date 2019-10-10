package reporting;

import reporting.entry.CompositeEntry;

public class Report {
    private final CompositeEntry rootEntry = new CompositeEntry();

    @Override
    public String toString() {
        return rootEntry.toString();
    }

    public CompositeEntry getRootEntry() {
        return rootEntry;
    }
}
