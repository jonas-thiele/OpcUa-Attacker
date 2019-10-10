package reporting.entry;


/**
 * Entry of report
 */
public interface Entry extends Iterable<Entry> {
    Entry addSubEntry(Entry subEntry);
    Entry addSubEntry(int index, Entry subEntry);
    void removeSubEntry(Entry subEntry);
    void removeSubEntry(int index);
    Entry getSubEntry(int index);
    int getSubEntryCount();
}
