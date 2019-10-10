package opcua.message.parts;

/**
 * Message Header for connection protocol messages (OPC UA Part 6, p. 55)
 */
public class ConnectionProtocolMessageHeader extends MessageHeader {
    public ConnectionProtocolMessageHeader(MessageType messageType, long messageSize) {
        super(messageType, IsFinal.FINAL_CHUNK, messageSize);
    }
}
