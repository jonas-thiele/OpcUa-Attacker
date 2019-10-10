package transport;

import opcua.context.Endpoint;
import opcua.context.LocalKeyPair;
import opcua.encoding.BinarySerializer;
import opcua.encoding.EncodingException;
import opcua.message.*;
import opcua.message.parts.*;
import opcua.security.MessageSecurityMode;
import opcua.security.SecurityPolicy;
import opcua.util.MessageUtility;

/**
 * Helper class for sending OPC UA messages
 */
public class MessageSender {

    /**
     * Sends an unsecured connection protocol message (for establishment of a transport connection)
     * @param message The message to send
     * @param connection Connection to the server
     * @throws TransportException
     */
    public static void sendMessage(ConnectionProtocolMessage message, Connection connection) throws TransportException {
        byte[] body = message.toBinary();
        byte[] header = new ConnectionProtocolMessageHeader(message.getMessageType(), body.length + 8).toBinary();
        byte[] payload = new BinarySerializer().putBytes(header).putBytes(body).get();
        connection.sendData(payload);
    }

    /**
     * Sends an unsecured OpenSecureChannel messages for the establishment of a secure channel (with NONE security configuration).
     * No certificate is send to the server
     * @param message The message
     * @param transportContext Transport context for the connection
     * @throws TransportException
     */
    public static void sendMessage(SecureConversationMessage message, TransportContext transportContext) throws TransportException {
        sendMessage(message, transportContext, LocalKeyPair.NULL);
    }

    /**
     * Sends an unsecured OpenSecureChannel messages for the establishment of a secure channel (with NONE security configuration)
     * @param message The message
     * @param transportContext Transport context for the connection
     * @param localKeyPair Contains the local certificate to send to the server
     * @throws TransportException
     */
    public static void sendMessage(SecureConversationMessage message, TransportContext transportContext, LocalKeyPair localKeyPair) throws TransportException {
        if(message.getMessageType() != MessageType.OPN) {
            throw new TransportException("Invalid message type for transport connection");
        }

        SecurityHeader securityHeader = new AsymmetricSecurityHeader(SecurityPolicy.NONE, localKeyPair.getCertificate(), null);
        SequenceHeader sequenceHeader = new SequenceHeader(transportContext.getAndIncrementSequenceNumber(), transportContext.getAndIncrementRequestId());
        byte[] body = message.toBinary();
        byte[] securityHeaderBytes;
        try {
            securityHeaderBytes = securityHeader.toBinary();
        } catch (EncodingException e) {
            throw new Error(e);
        }
        byte[] sequenceHeaderBytes = sequenceHeader.toBinary();

        int messageSize = 12 + securityHeaderBytes.length + sequenceHeaderBytes.length + body.length;
        SecureConversationMessageHeader messageHeader = new SecureConversationMessageHeader(message.getMessageType(), IsFinal.FINAL_CHUNK, messageSize, 0);

        byte[] payload = new BinarySerializer()
                .putBytes(messageHeader.toBinary())
                .putBytes(securityHeaderBytes)
                .putBytes(sequenceHeaderBytes)
                .putBytes(body)
                .get();

        transportContext.getConnection().sendData(payload);
    }

    /**
     * Sends a secure conversation message over a secure channel
     * @param message The message to send
     * @param secureChannelContext Context of the secure channel
     * @throws TransportException
     */
    public static void sendMessage(SecureConversationMessage message, SecureChannelContext secureChannelContext) throws TransportException {
        TransportContext transportContext = secureChannelContext.getTransportContext();
        SequenceHeader sequenceHeader = new SequenceHeader(transportContext.getAndIncrementSequenceNumber(), transportContext.getAndIncrementRequestId());
        SecurityHeader securityHeader = new SymmetricSecurityHeader(secureChannelContext.getSecurityToken().getTokenId());
        byte[] body = message.toBinary();

        byte[] securityHeaderBytes;
        try {
            securityHeaderBytes = securityHeader.toBinary();
        } catch (EncodingException e) {
            throw new Error(e);
        }
        byte[] sequenceHeaderBytes = sequenceHeader.toBinary();

        int messageSize = 12 + securityHeaderBytes.length + sequenceHeaderBytes.length + body.length;
        SecureConversationMessageHeader messageHeader = new SecureConversationMessageHeader(message.getMessageType(), IsFinal.FINAL_CHUNK, messageSize, secureChannelContext.getSecurityToken().getSecureChannelId());

        byte[] payload = new BinarySerializer()
                .putBytes(messageHeader.toBinary())
                .putBytes(securityHeaderBytes)
                .putBytes(sequenceHeaderBytes)
                .putBytes(body)
                .get();

        transportContext.getConnection().sendData(payload);
    }

    /**
     * Sends a secure message
     * @param message The message
     * @param transportContext Transport context of the connection
     * @param endpoint The remote endpoint
     * @param localKeyPair Local certificate
     * @throws TransportException
     */
    public static void sendMessage(SecureConversationMessage message, TransportContext transportContext, Endpoint endpoint, LocalKeyPair localKeyPair) throws TransportException {
        sendMessage(message, transportContext, endpoint, localKeyPair, endpoint.getMessageSecurityMode(), endpoint.getSecurityPolicy());
    }

    /**
     * Sends a secure message
     * @param message The message
     * @param transportContext Transport context of the connection
     * @param endpoint The remote endpoint
     * @param localKeyPair Local certificate
     * @param securityMode The security mode for this message
     * @param securityPolicy The security policy for this message
     * @throws TransportException
     */
    public static void sendMessage(SecureConversationMessage message, TransportContext transportContext, Endpoint endpoint,
                                   LocalKeyPair localKeyPair, MessageSecurityMode securityMode, SecurityPolicy securityPolicy) throws TransportException {
        switch (securityMode) {
            case NONE:
                sendMessage(message, transportContext, localKeyPair);
                break;
            case SIGN:
                throw new TransportException("Not implemented yet");
            case SIGN_AND_ENCRYPT:
                try {
                    byte[] signedEncrypted = MessageUtility.getSignedEncrypted(message, securityPolicy, localKeyPair, endpoint.getCertificate());
                    transportContext.getConnection().sendData(signedEncrypted);
                } catch (EncodingException e) {
                    throw new TransportException(e);
                }
                break;
            default:
                throw new TransportException("Invalid message security mode");
        }
    }

    /**
     * Sends raw data over a secure channel
     * @param payload The data to send
     * @param secureChannelContext The secure channel context
     * @throws TransportException
     */
    public static void sendBytes(byte[] payload, SecureChannelContext secureChannelContext) throws TransportException {
        secureChannelContext.getTransportContext().getConnection().sendData(payload);
    }

    /**
     * Sends raw data over a transport connection
     * @param payload The data to send
     * @param transportContext The transport context
     * @throws TransportException
     */
    public static void sendBytes(byte[] payload, TransportContext transportContext) throws TransportException {
        transportContext.getConnection().sendData(payload);
    }

    /**
     * Sends raw data over a plain connection
     * @param payload The data to send
     * @param connection The connection
     * @throws TransportException
     */
    public static void sendBytes(byte[] payload, Connection connection) throws TransportException {
        connection.sendData(payload);
    }
}
