package transport.tcp;

import opcua.context.Endpoint;
import opcua.context.StaticConfig;
import opcua.message.AcknowledgeMessage;
import opcua.message.HelloMessage;
import opcua.message.Message;
import opcua.message.parts.MessageType;
import transport.*;

/**
 * Contains utility functions for transport level client logic.
 */
public class TcpClientUtil {

    /**
     * Establishes a tcp transport connection with an OPC UA endpoint
     * @param endpoint Endpoint to connect to
     * @return A transport context containing the tcp connection and various parameters
     * @throws TransportException
     */
    public static TransportContext initializeTcpTransportConnection(Endpoint endpoint) throws TransportException {
        ClientTcpConnection connection = new ClientTcpConnection(endpoint.getHostName(), endpoint.getPort(), StaticConfig.TIMEOUT);
        connection.initialize();
        return initializeTcpTransportConnection(connection, endpoint);
    }

    /**
     * Establishes a tcp transport connection with an OPC UA endpoint using an existing connection
     * @param connection Connection to the server, the endpoint lives on
     * @param endpoint Endpoint to connect to
     * @return A transport context containing the tcp connection and various parameters
     * @throws TransportException
     */
    public static TransportContext initializeTcpTransportConnection(Connection connection, Endpoint endpoint) throws TransportException {
        HelloMessage hello = HelloMessage.fromConfig(endpoint.getEndpointUrl());
        MessageSender.sendMessage(hello, connection);
        Message response = MessageReceiver.receiveMessage(connection, endpoint.getMessageSecurityMode());
        if (response.getMessageType() != MessageType.ACK) {
            throw new TransportException("Unable to establish TCP connection");
        }
        AcknowledgeMessage ack = (AcknowledgeMessage)response;

        return new TransportContext(connection, ack.getProtocolVersion(), ack.getReceiveBufferSize(), ack.getSendBufferSize(), ack.getMaxMessageSize(), ack.getMaxChunkCount());
    }
}
