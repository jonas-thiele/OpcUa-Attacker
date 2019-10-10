package opcua.message.parts;


/**
 * Message Type for message header (OPC UA Part 6, p. 47, p. 55)
 */
public enum MessageType {

    MSG(false),    // normal Message
    OPN(false),    // OpenSecureChannel
    CLO(false),    // CloseSecureChannel

    HEL(true),    // Hello
    ACK(true),    // Acknowledge
    ERR(true),    // Error
    RHE(true);    // ReverseHello


    private boolean isConnectionProtocolMessage;

    MessageType(boolean isConnectionProtocolMessage) {
        this.isConnectionProtocolMessage = isConnectionProtocolMessage;
    }

    /**
     * Return the 3 bytes that identify the message type.
     */
    public byte[] getIdentifier() {
        return this.toString().getBytes();
    }

    public static MessageType fromIdentifier(byte[] identifier) {
        return MessageType.valueOf(new String(identifier));
    }

    public boolean isConnectionProtocolMessage() {
        return isConnectionProtocolMessage;
    }

    public boolean isError() { return this == MessageType.ERR; }
}
