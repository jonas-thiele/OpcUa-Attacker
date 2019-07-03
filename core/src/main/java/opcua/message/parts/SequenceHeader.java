package opcua.message.parts;

import opcua.util.BinarySerializer;

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
