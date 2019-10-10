package opcua.util;

import opcua.context.Endpoint;
import opcua.message.GetEndpointsRequest;
import opcua.message.GetEndpointsResponse;
import opcua.message.Message;
import opcua.message.parts.MessageType;
import opcua.model.type.EndpointDescription;
import opcua.security.MessageSecurityMode;
import transport.*;
import transport.tcp.TcpClientUtil;

/**
 * Implements common OPC UA message flows
 */
public class CommonMessageFlows {

    /**
     * Message flow to retrieve endpoint descriptions of an OPC UA server
     * @param endpoint The endpoint to send the GetEndpointsRequest to
     * @return An Array of endpoint descriptions
     * @throws TransportException
     */
    public static EndpointDescription[] retrieveEndpointDescriptions(Endpoint endpoint) throws TransportException {
        TransportContext transportContext = TcpClientUtil.initializeTcpTransportConnection(endpoint);
        SecureChannelContext secureChannelContext = SecureChannelUtil.establishSecureChannel(transportContext);

        GetEndpointsRequest getEndpointsRequest = new GetEndpointsRequest(endpoint.getEndpointUrl(), new String[0], new String[0]);
        MessageSender.sendMessage(getEndpointsRequest, secureChannelContext);
        Message response = MessageReceiver.receiveMessage(secureChannelContext);
        MessageUtility.throwIfUnexpectedType(response, GetEndpointsResponse.class);

        return ((GetEndpointsResponse) response).getEndpoints();
    }
}
