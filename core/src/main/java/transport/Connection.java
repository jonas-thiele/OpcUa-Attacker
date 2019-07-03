package transport;

import java.io.IOException;


public interface Connection {
    void initialize() throws IOException;

    void sendData(byte[] data) throws IOException;

    byte[] receiveData() throws IOException;

    byte[] receiveData(int length) throws IOException;

    void close() throws IOException;
}
