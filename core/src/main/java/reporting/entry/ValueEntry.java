package reporting.entry;

public class ValueEntry<T> extends LeafEntry {
    private String key;
    private T value;

    public ValueEntry(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public String getValueAsString() {
        return value.toString();
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return key + ": " + value.toString() + "\n";
    }
}
