package opcua.message.parts;

import opcua.encoding.BinarySerializer;
import opcua.encoding.MessageInputStream;

import java.io.IOException;

/**
 * Sequence Header (OPC UA Part 6, p. 49)
 */
public class SequenceHeader {
    private long sequenceNumber;
    private long requestId;

    public SequenceHeader(long sequenceNumber, long requestId) {
        this.sequenceNumber = sequenceNumber;
        this.requestId = requestId;
    }

    public byte[] toBinary() {
        return new BinarySerializer()
                .putUInt32(sequenceNumber)
                .putUInt32(requestId)
                .get();
    }

    public static SequenceHeader constructFromBinary(MessageInputStream stream) throws IOException {
        return new SequenceHeader(
                stream.readUInt32(),
                stream.readUInt32()
        );
    }


    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }
}
