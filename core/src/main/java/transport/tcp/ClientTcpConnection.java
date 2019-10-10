package transport.tcp;

import transport.Connection;
import org.apache.log4j.Logger;
import transport.TransportException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Represents the connection to a server via TCP
 */
public class ClientTcpConnection implements Connection {
    private static final Logger logger = Logger.getRootLogger();

    Socket socket;
    private final String hostName;
    private final int port;
    private final int timeout;

    /**
     * Constructor
     * @param hostName Host name of the server to connect to
     * @param port Port of the server to connect to
     * @param timeout Tcp timeout
     */
    public ClientTcpConnection(String hostName, int port, int timeout) {
        this.hostName = hostName;
        this.port = port;
        this.timeout = timeout;
    }

    /**
     * Establishes socket connection with server
     * @throws TransportException
     */
    public void initialize() throws TransportException {
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(hostName, port), timeout);
        } catch (IOException e) {
            throw new TransportException(e);
        }
    }

    /**
     * Send data to the server
     * @param data The data to send
     * @throws TransportException
     */
    public void sendData(byte[] data) throws TransportException {
        if(socket == null || socket.isClosed()) {
            throw new TransportException("Connection not active");
        }

        try {
            socket.getOutputStream().write(data);
            socket.getOutputStream().flush();
        } catch (IOException e) {
            throw new TransportException(e);
        }
    }

    /**
     * Receives data from the server. Blocks if insufficient data is available
     * @param length How much data to receive
     * @return A byte array of length length
     * @throws TransportException
     */
    public byte[] receiveData(int length) throws TransportException {
        if(socket == null || socket.isClosed()) {
            throw new TransportException("Connection not active");
        }

        try {
            return socket.getInputStream().readNBytes(length);
        } catch (IOException e) {
            throw new TransportException(e);
        }
    }

    /**
     * Closes the connection
     * @throws TransportException
     */
    public void close() throws TransportException {
        try {
            socket.close();
        } catch (IOException e) {
            throw new TransportException(e);
        }
    }
}
