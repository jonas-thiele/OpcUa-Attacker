package opcua.message;
import opcua.message.parts.MessageType;
import opcua.util.MessageInputStream;

import java.io.IOException;

public class NotImplementedMessage extends Message {

    private MessageType messageType;

    public NotImplementedMessage(MessageType messageType) {
        this.messageType = messageType;
    }

    public NotImplementedMessage() {}

    @Override
    public byte[] toBinary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public static NotImplementedMessage constructFromBinary(MessageInputStream stream, MessageType messageType) throws IOException {
        return new NotImplementedMessage(messageType);

    }
}
