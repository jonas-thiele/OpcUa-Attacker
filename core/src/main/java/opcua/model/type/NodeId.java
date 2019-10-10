package opcua.model.type;

import java.util.Objects;

/**
 * NodeId (OPC UA Part 6, pp. 11-13)
 */
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

        public static DataEncoding fromFlag(byte flag) {
            switch(flag) {
                case (byte)0x0: return DataEncoding.TWO_BYTE;
                case (byte)0x1: return DataEncoding.FOUR_BYTE;
                case (byte)0x2: return DataEncoding.NUMERIC;
                case (byte)0x3: return DataEncoding.STRING;
                case (byte)0x4: return DataEncoding.GUID;
                default: return  DataEncoding.BYTE_STRING;
            }
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

    public long getLongValue() {
        return (long)value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeId nodeId = (NodeId) o;
        return namespace == nodeId.namespace &&
                type == nodeId.type &&
                Objects.equals(value, nodeId.value);
    }

}
