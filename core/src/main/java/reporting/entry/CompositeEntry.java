package reporting.entry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Entry with sub entries
 */
public class CompositeEntry implements Entry {
    private final List<Entry> subEntries = new ArrayList<>();

    @Override
    public Entry addSubEntry(Entry subEntry) {
        subEntries.add(subEntry);
        return this;
    }

    @Override
    public Entry addSubEntry(int index, Entry subEntry) {
        subEntries.add(index, subEntry);
        return this;
    }

    @Override
    public void removeSubEntry(Entry subEntry) {
        subEntries.remove(subEntry);
    }

    @Override
    public void removeSubEntry(int index) {
        subEntries.remove(index);
    }

    @Override
    public Entry getSubEntry(int index) {
        return subEntries.get(index);
    }

    @Override
    public int getSubEntryCount() {
        return subEntries.size();
    }

    @Override
    public Iterator<Entry> iterator() {
        return subEntries.iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(Entry subEntry : this) {
            sb.append(subEntry.toString());
        }
        if(this.getSubEntryCount() == 0) {
            sb.append("no entries\n");
        }
        return sb.toString();
    }
}
