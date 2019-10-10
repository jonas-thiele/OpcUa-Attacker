package opcua.message;

import opcua.message.parts.MessageType;

public abstract class ConnectionProtocolMessage extends Message {
    private final MessageType messageType;

    ConnectionProtocolMessage(MessageType messageType) {
        this.messageType = messageType;
    }

    public abstract byte[] toBinary();

    public MessageType getMessageType() {
        return messageType;
    }

}
