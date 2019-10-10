package reporting.entry;

public class Group extends CompositeEntry {
    private String name;

    public Group(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + ":\n" + super.toString().indent(4);
    }
}
