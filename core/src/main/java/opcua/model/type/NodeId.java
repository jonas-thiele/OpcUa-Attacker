package opcua.model.type;

public class NodeId {

    public enum Type {
        NUMERIC,    //Only NUMERIC is implemented so far
        STRING,
        GUID,
        OPAQUE
    }

    public enum DataEncoding {
        TWO_BYTE((byte)0x0),
        FOUR_BYTE((byte)0x1),
        NUMERIC((byte)0x2),
        STRING((byte)0x3),
        GUID((byte)0x4),
        BYTE_STRING((byte)0x5);

        private byte flag;

        DataEncoding(byte flag) {
            this.flag = flag;
        }

        public byte getFlag() {
            return flag;
        }
    }

    public static final NodeId NUMERIC_NULL = new NodeId(0, 0);

    private final Type type;
    private final int namespace;
    private final Object value;

    public NodeId(int namespace, long value) {
        this.type = Type.NUMERIC;
        this.namespace = namespace;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public int getNamespace() {
        return namespace;
    }

    public Object getValue() {
        return value;
    }
}
