package transport;

import opcua.encoding.*;
import opcua.message.Message;
import opcua.message.parts.*;
import opcua.security.MessageSecurityMode;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Helper class for the reception of messages
 */
public class MessageReceiver {
    private static final Logger LOGGER = Logger.getRootLogger();

    /**
     * Receives a message on a secure channel
     * @param secureChannelContext Context of the secure channel
     * @return Received message
     * @throws TransportException
     */
    public static Message receiveMessage(SecureChannelContext secureChannelContext) throws  TransportException {
        return receiveMessage(secureChannelContext.getTransportContext().getConnection(), secureChannelContext.getSecurityMode());
    }

    /**
     * Receives a message
     * @param connection Connection to server
     * @param securityMode Security mode for decryption/verification (only NONE is supported thus far)
     * @return Received message
     * @throws TransportException
     */
    public static Message receiveMessage(Connection connection, MessageSecurityMode securityMode) throws TransportException {
        try {
            byte[] msgHeader = connection.receiveData(8);
            MessageType messageType = MessageType.fromIdentifier(Arrays.copyOfRange(msgHeader, 0, 3));
            IsFinal isFinal = IsFinal.fromIdentifier(Arrays.copyOfRange(msgHeader, 3, 4));
            long chunkSize = DataTypeConverter.bytesToUInt32LE(Arrays.copyOfRange(msgHeader, 4, 8));

            if (messageType.isConnectionProtocolMessage()) {
                //Read header
                long bodySize = chunkSize - 8;
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                buffer.writeBytes(connection.receiveData((int) bodySize));
                return Message.constructFromBinary(messageType, new MessageInputStream(new ByteArrayInputStream(buffer.toByteArray())));
            } else {
                ChunkAssembler chunkAssembler;
                switch (securityMode) {
                    case SIGN: throw new TransportException("Not supported yet");
                    case SIGN_AND_ENCRYPT: throw new TransportException("Not supported yet");
                    default:
                        chunkAssembler = new PlainChunkAssembler();
                        break;
                }

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                buffer.write(msgHeader);
                buffer.write(connection.receiveData((int) chunkSize - 8));
                chunkAssembler.putChunk(buffer.toByteArray());
                while (chunkAssembler.moreChunksRequired()) {
                    msgHeader = connection.receiveData(8);
                    chunkSize = DataTypeConverter.bytesToUInt32LE(Arrays.copyOfRange(msgHeader, 4, 8));
                    buffer = new ByteArrayOutputStream();
                    buffer.write(msgHeader);
                    buffer.write(connection.receiveData((int) chunkSize - 8));
                    chunkAssembler.putChunk(buffer.toByteArray());
                }
                return chunkAssembler.retrieveMessage();
            }
        } catch (EncodingException | IOException e) {
            throw new TransportException(e);
        }
    }
}
