package opcua.encoding;

import opcua.message.Message;

/**
 * Assembles a message from a number of chunks
 */
public interface ChunkAssembler {

    /**
     * Feeds a chunk
     * @param chunk Chunk data
     * @return True, if more chunks are required to complete message
     * @throws EncodingException
     */
    boolean putChunk(byte[] chunk) throws EncodingException;

    /**
     * Returns, whether more chunks are required to complete message
     */
    boolean moreChunksRequired();

    /**
     * Retrieves the completed message
     * @return The message
     * @throws EncodingException If message is not complete yet
     */
    Message retrieveMessage() throws EncodingException;
}
