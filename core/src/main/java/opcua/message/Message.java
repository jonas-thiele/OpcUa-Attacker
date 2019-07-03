package opcua.message;

import opcua.message.parts.MessageType;
import opcua.util.MessageInputStream;

import java.io.IOException;

public abstract class Message {

    public abstract byte[] toBinary();
    public abstract MessageType getMessageType();

    public static Message constructFromBinary(MessageType messageType, MessageInputStream stream) throws IOException {
        switch (messageType) {
            case HEL: return HelloMessage.constructFromBinary(stream);
            case ACK: return AcknowledgeMessage.constructFromBinary(stream);
            case RHE: return ReverseHelloMessage.constructFromBinary(stream);
            case ERR: return ErrorMessage.constructFromBinary(stream);
            default: return NotImplementedMessage.constructFromBinary(stream, messageType);
        }
    }

}
