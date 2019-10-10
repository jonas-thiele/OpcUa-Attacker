package reporting.entry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ValueEntry for dates
 */
public class DateTimeEntry extends ValueEntry<LocalDateTime> {

    public DateTimeEntry(String key, LocalDateTime value) {
        super(key, value);
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return getKey() + ": " + getValue().format(formatter) + "\n";
    }
}
