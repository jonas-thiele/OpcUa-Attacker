package reporting.entry;

import java.time.Duration;

/**
 * Value entry for a duration
 */
public class DurationEntry extends ValueEntry<Duration> {
    public DurationEntry(String key, Duration value) {
        super(key, value);
    }

    @Override
    public String toString() {
        return getKey() + ": " + getValue().toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase()  + "\n";
    }
}
