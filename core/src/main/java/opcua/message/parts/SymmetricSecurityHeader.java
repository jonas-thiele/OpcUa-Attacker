package opcua.message.parts;

import opcua.encoding.BinarySerializer;
import opcua.encoding.EncodingException;
import opcua.encoding.MessageInputStream;
import opcua.model.type.SecurityToken;

import java.io.IOException;

/**
 * Symmetric Security Header (OPC UA Part 6, p. 49)
 */
public class SymmetricSecurityHeader implements SecurityHeader {
    private long tokenId;

    public SymmetricSecurityHeader() { }

    public SymmetricSecurityHeader(long tokenId) {
        this.tokenId = tokenId;
    }

    @Override
    public byte[] toBinary() throws EncodingException {
        return new BinarySerializer().putUInt32(tokenId).get();
    }

    public static SymmetricSecurityHeader constructFromBinary(MessageInputStream stream) throws IOException {
        long tokenId = stream.readUInt32();
        return new SymmetricSecurityHeader(tokenId);
    }

    public long getTokenId() {
        return tokenId;
    }

    public void setTokenId(long tokenId) {
        this.tokenId = tokenId;
    }
}
