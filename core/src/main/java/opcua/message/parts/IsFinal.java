package opcua.message.parts;

/**
 * Is Final field for message header (OPC UA Part 6, p. 47)
 */
public enum IsFinal {
    INTERMEDIATE_CHUNK((byte)'C'),
    FINAL_CHUNK((byte)'F'),
    FINAL_CHUNK_ABORT((byte)'A');

    private byte value;

    IsFinal(byte value) {
        this.value = value;
    }

    public byte getIdentifier() {
        return value;
    }

    public static IsFinal fromIdentifier(byte[] identifier) {
        switch(identifier[0]) {
            case (byte)'C': return IsFinal.INTERMEDIATE_CHUNK;
            case (byte)'F': return IsFinal.FINAL_CHUNK;
            default: return IsFinal.FINAL_CHUNK_ABORT;
        }
    }
}
