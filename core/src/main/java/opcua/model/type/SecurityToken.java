package opcua.model.type;

import opcua.encoding.BinarySerializer;
import opcua.encoding.MessageInputStream;

import java.io.IOException;
import java.time.Instant;

/**
 * SecurityToken
 */
public class SecurityToken {
    private long secureChannelId;
    private long tokenId;
    private Instant createdAt;
    private long revisedLifetime;

    public SecurityToken(long secureChannelId, long tokenId, Instant createdAt, long revisedLifetime) {
        this.secureChannelId = secureChannelId;
        this.tokenId = tokenId;
        this.createdAt = createdAt;
        this.revisedLifetime = revisedLifetime;
    }

    public byte[] toBinary() {
        return new BinarySerializer()
                .putUInt32(secureChannelId)
                .putUInt32(tokenId)
                .putDateTime(createdAt)
                .putUInt32(revisedLifetime)
                .get();
    }

    public static SecurityToken constructFromBinary(MessageInputStream stream) throws IOException {
        return new SecurityToken(
                stream.readUInt32(),
                stream.readUInt32(),
                stream.readDateTime(),
                stream.readUInt32()
        );
    }

    public SecurityToken() { }

    public long getSecureChannelId() {
        return secureChannelId;
    }

    public void setSecureChannelId(long secureChannelId) {
        this.secureChannelId = secureChannelId;
    }

    public long getTokenId() {
        return tokenId;
    }

    public void setTokenId(long tokenId) {
        this.tokenId = tokenId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public long getRevisedLifetime() {
        return revisedLifetime;
    }

    public void setRevisedLifetime(long revisedLifetime) {
        this.revisedLifetime = revisedLifetime;
    }
}
