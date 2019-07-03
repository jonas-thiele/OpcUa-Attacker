package opcua.message.parts;

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
}
