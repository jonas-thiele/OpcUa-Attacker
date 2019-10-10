package reporting.entry;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class LeafEntry implements Entry {

    @Override
    public Entry addSubEntry(Entry subEntry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entry addSubEntry(int index, Entry subEntry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeSubEntry(Entry subEntry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeSubEntry(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entry getSubEntry(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSubEntryCount() {
        return 0;
    }

    @Override
    public Iterator<Entry> iterator() {
        return Collections.emptyIterator();
    }
}
