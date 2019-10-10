package transport.tcp;

import transport.TransportException;

import java.io.IOException;

/**
 * Like ClientTcpConnection with additional support for high precision timing
 */
public class HighPrecisionTimingClientTcpConnection extends ClientTcpConnection {

    /**
     * Constructor
     * @param hostName Host name of the server to connect to
     * @param port Port of the server to connect to
     * @param timeout Tcp timeout
     */
    public HighPrecisionTimingClientTcpConnection(String hostName, int port, int timeout) {
        super(hostName, port, timeout);
    }

    /**
     * Sends data and returns the elapsed time until the first byte of a response was received. This method will busy wait
     * until a response is received and might potentially not return at all!
     */
    public long timedSendData(byte[] data) throws TransportException {
        super.sendData(data);
        long pre = System.nanoTime();
        //Busy waiting until respond data is available
        try {
            while (socket.getInputStream().available() == 0) {}
        } catch (IOException e) {}  // Socket was closed by server (we still can measure this)
        long post = System.nanoTime();
        return post - pre;
    }
}
