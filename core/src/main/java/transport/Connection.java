package transport;

import java.io.IOException;


public interface Connection {
    void initialize() throws TransportException;
    void sendData(byte[] data) throws TransportException;
    byte[] receiveData(int length) throws TransportException;
    void close() throws TransportException;
}
