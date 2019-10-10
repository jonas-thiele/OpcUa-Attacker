package opcua.encoding;

import io.netty.handler.codec.DecoderException;
import opcua.message.Message;
import opcua.message.parts.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;

/**
 * ChunkAssembler for unsecured messages
 */
public class PlainChunkAssembler implements ChunkAssembler {
    private boolean moreChunksRequired;
    private ByteArrayOutputStream buffer;
    private MessageType messageType;
    private Message message;

    /**
     * Constructor
     */
    public PlainChunkAssembler() {
        this.moreChunksRequired = true;
        this.buffer = new ByteArrayOutputStream();
    }

    @Override
    public boolean putChunk(byte[] chunk) throws EncodingException {
        MessageInputStream stream = new MessageInputStream(chunk);

        try {
            MessageType messageType = MessageType.fromIdentifier(stream.readBytes(3));
            if(this.messageType == null) {
                this.messageType = messageType;
            }
            if(this.messageType != messageType) {
                throw new EncodingException("MessageType of chunk does not match");
            }

            IsFinal isFinal = IsFinal.fromIdentifier(stream.readBytes(1));
            if(isFinal == IsFinal.FINAL_CHUNK) {
                moreChunksRequired = false;
            }
            long chunkSize = stream.readUInt32();
            long secureChannelId = stream.readUInt32();

            SecurityHeader securityHeader = messageType == MessageType.OPN ?
                    AsymmetricSecurityHeader.constructFromBinary(stream) :
                    SymmetricSecurityHeader.constructFromBinary(stream);
            SequenceHeader sequenceHeader = SequenceHeader.constructFromBinary(stream);
            buffer.writeBytes(stream.readAllBytes());
        }
        catch (IOException | CertificateException e) {
            throw new EncodingException("Unable to decode chunk", e);
        }

        if(!moreChunksRequired) {
            assembleMessage();
        }

        return moreChunksRequired;
    }

    @Override
    public boolean moreChunksRequired() {
        return moreChunksRequired;
    }

    @Override
    public Message retrieveMessage() throws EncodingException {
        if(moreChunksRequired || message == null) {
            throw new EncodingException("More chunks are required in order to assembe message");
        }
        return message;
    }


    private void assembleMessage() throws EncodingException {
        MessageInputStream stream = new MessageInputStream(buffer.toByteArray());
        this.message = Message.constructFromBinary(this.messageType, stream);
    }
}
