package opcua.message;

import opcua.encoding.MessageInputStream;
import opcua.model.type.NodeId;
import opcua.model.type.ObjectIds;

import java.io.IOException;

public abstract class SecureConversationMessage extends Message {
    public static Message constructFromBinary(MessageInputStream stream) throws IOException {
        NodeId nodeId = stream.readNodeId();

        if(nodeId.equals(ObjectIds.GET_ENDPOINTS_RESPONSE)) {
            return GetEndpointsResponse.constructFromBinary(stream);
        }
        throw new Error("Unsupported message");
    }
}
