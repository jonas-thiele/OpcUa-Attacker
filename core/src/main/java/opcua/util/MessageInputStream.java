package opcua.util;

import opcua.message.Message;
import org.apache.log4j.Logger;
import transport.Connection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MessageInputStream {
    private static final Logger LOGGER = Logger.getRootLogger();

    private ByteArrayInputStream stream;

    public MessageInputStream(ByteArrayInputStream stream) {
        this.stream = stream;
    }


    public byte[] readBytes(int len) throws IOException {
        return stream.readNBytes(len);
    }

    public void skipBytes(int len) throws IOException {
        stream.skipNBytes(len);
    }

    public boolean readBoolean() throws IOException {
        return DataTypeConverter.bytesToBoolean(stream.readNBytes(1));
    }

    public int readInt32() throws IOException {
        return DataTypeConverter.bytesToInt32LE(stream.readNBytes(4));
    }

    public long readUInt32() throws IOException {
        return DataTypeConverter.bytesToUInt32LE(stream.readNBytes(4));
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
}
