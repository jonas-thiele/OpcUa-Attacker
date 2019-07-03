package opcua.message.parts;

public class ConnectionProtocolMessageHeader extends MessageHeader {

    public ConnectionProtocolMessageHeader(MessageType messageType, long messageSize) {
        super(messageType, IsFinal.FINAL_CHUNK, messageSize);
    }

}
