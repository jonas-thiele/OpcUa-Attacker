package opcua.message;

import opcua.message.parts.MessageType;
import opcua.encoding.BinarySerializer;
import opcua.encoding.MessageInputStream;

import java.io.IOException;

/**
 * Error message of the OPC UA connection protocol (OPC UA Part 6, p.56)
 */
public class ErrorMessage extends ConnectionProtocolMessage {
    private long error;
    private String reason;

    public ErrorMessage(long error, String reason) {
        super(MessageType.ERR);
        this.error = error;
        this.reason = reason;
    }

    public ErrorMessage() {
        super(MessageType.ERR);
    }

    @Override
    public byte[] toBinary() {
        return new BinarySerializer()
                .putUInt32(error)
                .putString(reason)
                .get();
    }

    public static ErrorMessage constructFromBinary(MessageInputStream stream) throws IOException {
        ErrorMessage msg = new ErrorMessage();
        msg.setError(stream.readUInt32());
        msg.setReason(stream.readString());
        return msg;
    }


    public long getError() {
        return error;
    }

    public void setError(long error) {
        this.error = error;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.ERR;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("ErrorMessage:")
                .append("\n   Error: 0x").append(Long.toHexString(error))
                .append("\n   Reason: ").append(reason)
                .toString();
    }
}
