package opcua.message.parts;

import opcua.util.BinarySerializer;

public class SecureConversationMessageHeader extends MessageHeader {

    private long secureChannelId;


    public SecureConversationMessageHeader(MessageType messageType, IsFinal isFinal, long messageSize, long secureChannelId) {
        super(messageType, isFinal, messageSize);
        this.secureChannelId = secureChannelId;
    }


    @Override
    public byte[] toBinary() {
        return new BinarySerializer()
                .putBytes(super.toBinary())
                .putUInt32(secureChannelId)
                .get();
    }


    public long getSecureChannelId() {
        return secureChannelId;
    }

    public void setSecureChannelId(long secureChannelId) {
        this.secureChannelId = secureChannelId;
    }
}
