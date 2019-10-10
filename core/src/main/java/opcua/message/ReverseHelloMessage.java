package opcua.message;

import opcua.message.parts.MessageType;
import opcua.encoding.BinarySerializer;
import opcua.encoding.MessageInputStream;

import java.io.IOException;

/**
 * ReverseHello message of the OPC UA connection protocol (OPC UA Part 6, p.56)
 */
public class ReverseHelloMessage extends ConnectionProtocolMessage {
    private String serverUri;
    private String endpointUrl;

    public ReverseHelloMessage(String serverUri, String endpointUrl) {
        super(MessageType.RHE);
        this.serverUri = serverUri;
        this.endpointUrl = endpointUrl;
    }

    public ReverseHelloMessage() {
        super(MessageType.RHE);
    }

    @Override
    public byte[] toBinary() {
        return new BinarySerializer()
                .putString(serverUri)
                .putString(endpointUrl)
                .get();
    }

    public static ReverseHelloMessage constructFromBinary(MessageInputStream stream) throws IOException {
        ReverseHelloMessage msg = new ReverseHelloMessage();
        msg.setServerUri(stream.readString());
        msg.setEndpointUrl(stream.readString());
        return msg;
    }

    public String getServerUri() {
        return serverUri;
    }

    public void setServerUri(String serverUri) {
        this.serverUri = serverUri;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.RHE;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("ReverseHelloMessage:")
                .append("\n   ServerUri: ").append(serverUri)
                .append("\n   EndpointUrl: ").append(endpointUrl)
                .toString();
    }
}
