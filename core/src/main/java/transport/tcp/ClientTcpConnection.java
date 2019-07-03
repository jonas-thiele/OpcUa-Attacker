package transport.tcp;

import transport.Connection;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientTcpConnection implements Connection {
    private static final Logger logger = Logger.getRootLogger();

    private Socket socket;
    private final String hostName;
    private final int port;
    private final int timeout;

    public ClientTcpConnection(String hostName, int port, int timeout) {
        this.hostName = hostName;
        this.port = port;
        this.timeout = timeout;
    }

    public void initialize() throws IOException{
        socket = new Socket();
        socket.connect(new InetSocketAddress(hostName, port), timeout);
    }

    public void sendData(byte[] data) throws IOException {
        if(socket == null || socket.isClosed()) {
            throw new IOException("Connection not active");
        }

        socket.getOutputStream().write(data);
        socket.getOutputStream().flush();
    }

    public byte[] receiveData(int length) throws IOException {
        if(socket == null || socket.isClosed()) {
            throw new IOException("Connection not active");
        }

        return socket.getInputStream().readNBytes(length);
    }

    public byte[] receiveData() throws IOException {
        if(socket == null || socket.isClosed()) {
            throw new IOException("Connection is not active");
        }

        return null;
    }

    public void close() throws IOException {
        if(socket == null) {
            throw new IOException("Socket was not initialized");
        }

        socket.close();
    }
}
