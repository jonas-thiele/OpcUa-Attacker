package opcua.message.parts;

import opcua.message.Message;

public enum MessageType {

    /**
     * TODO: OPC UA 6.7.2.2 Secure Conversation  MessageType
     */
    MSG(false),    // normal Message
    OPN(false),    // OpenSecureChannel
    CLO(false),    // CloseSecureChannel

    /**
     * TODO: OPC UA 7.1.2.1 Connection Protocol MessageType
     */
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
}
