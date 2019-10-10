package opcua.encoding;

import opcua.model.type.NodeId;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;


/**
 * Stream to read data types from a bytes stream of an binary encoded message
 */
public class MessageInputStream {
    private static final Logger LOGGER = Logger.getRootLogger();

    private ByteArrayInputStream stream;

    public MessageInputStream(ByteArrayInputStream stream) {
        this.stream = stream;
    }

    public MessageInputStream(byte[] data) {
        this.stream = new ByteArrayInputStream(data);
    }


    public byte[] readBytes(int len) throws IOException {
        return stream.readNBytes(len);
    }

    public byte[] readAllBytes() throws IOException {
        return stream.readAllBytes();
    }

    public void skipBytes(int len) throws IOException {
        stream.skipNBytes(len);
    }

    public byte readByte() throws IOException {
        return readBytes(1)[0];
    }

    public int readUByte() throws IOException {
        byte signed = readByte();
        return signed & 0xFF;
    }

    public boolean readBoolean() throws IOException {
        return DataTypeConverter.bytesToBoolean(stream.readNBytes(1));
    }

    public int readUInt16() throws IOException {
        return DataTypeConverter.bytesToUInt16LE(stream.readNBytes(2));
    }

    public int readInt32() throws IOException {
        return DataTypeConverter.bytesToInt32LE(stream.readNBytes(4));
    }

    public long readUInt32() throws IOException {
        return DataTypeConverter.bytesToUInt32LE(stream.readNBytes(4));
    }

    public long readInt64() throws IOException {
        return DataTypeConverter.bytesToInt64LE(stream.readNBytes(8));
    }

    public String readString() throws IOException {
        int len = readInt32();
        if(len == -1) {
            return null;
        }
        return new String(readBytes(len));
    }

    public byte[] readByteArray() throws IOException {
        int len = readInt32();
        if(len == -1) {
            return null;
        }
        return readBytes(len);
    }

    public Instant readDateTime() throws IOException {
        final long OffsetToGregorianCalendarZero = 116444736000000000L;

        long value = readInt64();
        if(value == 0) {
            return null;
        }
        return Instant.ofEpochMilli((value - OffsetToGregorianCalendarZero) / 10000);
    }

    public NodeId readNodeId() throws IOException {
        NodeId.DataEncoding encoding = NodeId.DataEncoding.fromFlag(readByte());
        switch(encoding) {
            case TWO_BYTE: {
                int value = readByte();
                return new NodeId(0, value);
            }
            case FOUR_BYTE: {
                int namespace = readByte();
                int value = readUInt16();
                return new NodeId(namespace, value);
            }
            case NUMERIC: {
                int namespace = readUInt16();
                long value = readUInt32();
                return new NodeId(namespace, value);
            }
            default: throw new IOException("Unsupported NodeId type");
        }
    }

    public String readLocalizedText() throws IOException {
        byte encodingMask = readByte();
        if((encodingMask & (byte)0x01) == (byte)0x01) {
            readString(); //Discard locale
        }
        if((encodingMask & (byte)0x02) == (byte)0x02) {
            return readString();
        }
        return null;
    }

    public <E extends Enum<E>> E readEnumeration(Class<E> elementType) throws IOException {
        int index = (int)readUInt32();
        return elementType.getEnumConstants()[index];
    }

    public String[] readStringArray() throws IOException {
        int length = (int)readUInt32();
        if(length == -1) {
            return null;
        }
        String[] strings = new String[length];
        for(int i=0; i<length; i++) {
            strings[i] = readString();
        }
        return strings;
    }
}
