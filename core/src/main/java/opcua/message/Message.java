package opcua.message;

import opcua.encoding.EncodingException;
import opcua.message.parts.MessageType;
import opcua.encoding.MessageInputStream;

import java.io.IOException;

public abstract class Message {
    public abstract byte[] toBinary();
    public abstract MessageType getMessageType();

    public static Message constructFromBinary(MessageType messageType, MessageInputStream stream) throws EncodingException {
        try {
        switch (messageType) {
            case HEL: return HelloMessage.constructFromBinary(stream);
            case ACK: return AcknowledgeMessage.constructFromBinary(stream);
            case RHE: return ReverseHelloMessage.constructFromBinary(stream);
            case ERR: return ErrorMessage.constructFromBinary(stream);
            case OPN: return OpenSecureChannelResponse.constructFromBinary(stream);
            default: return SecureConversationMessage.constructFromBinary(stream);
        }
        } catch (IOException e) {
            throw new EncodingException(e);
        }
    }

}
