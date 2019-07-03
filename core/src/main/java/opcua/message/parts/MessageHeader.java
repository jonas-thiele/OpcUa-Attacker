package opcua.message.parts;

import opcua.util.BinarySerializer;

public abstract class MessageHeader {

    private MessageType messageType;
    private IsFinal isFinal;
    private long messageSize;


    public MessageHeader(MessageType messageType, IsFinal isFinal, long messageSize) {
        this.messageType = messageType;
        this.isFinal = isFinal;
        this.messageSize = messageSize;
    }


    public byte[] toBinary() {
        return new BinarySerializer()
                .putBytes(messageType.getIdentifier())
                .putByte(isFinal.getIdentifier())
                .putUInt32(messageSize)
                .get();
    }


    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public IsFinal getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(IsFinal isFinal) {
        this.isFinal = isFinal;
    }

    public long getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(long messageSize) {
        this.messageSize = messageSize;
    }
}
