package opcua.message;

import opcua.encoding.BinarySerializer;
import opcua.encoding.MessageInputStream;
import opcua.message.parts.MessageType;
import opcua.message.parts.ResponseHeader;
import opcua.model.type.EndpointDescription;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;

/**
 * GetEndpoints Response (OPC UA Part 4, p. 15)
 */
public class GetEndpointsResponse extends SecureConversationMessage {
    private ResponseHeader responseHeader;
    private EndpointDescription[] endpoints;

    public GetEndpointsResponse() { }

    public GetEndpointsResponse(ResponseHeader responseHeader, EndpointDescription[] endpoints) {
        this.responseHeader = responseHeader;
        this.endpoints = endpoints;
    }

    @Override
    public byte[] toBinary() {
        BinarySerializer serializer = new BinarySerializer();
        serializer.putBytes(responseHeader.toBinary());
        serializer.putUInt32(endpoints.length);
        for(EndpointDescription endpoint : endpoints) {
            try {
                serializer.putBytes(endpoint.toBinary());
            } catch (CertificateEncodingException e) {
                throw new Error(e);
            }
        }
        return serializer.get();
    }

    public static GetEndpointsResponse constructFromBinary(MessageInputStream stream) throws IOException {
        ResponseHeader responseHeader = ResponseHeader.constructFromBinary(stream);
        int numEndpoints = (int)stream.readUInt32();
        EndpointDescription[] endpointDescriptions = new EndpointDescription[numEndpoints];
        for(int i=0; i<numEndpoints; i++) {
            try {
                endpointDescriptions[i] = EndpointDescription.constructFromBinary(stream);
            } catch (CertificateException e) {
                throw new IOException(e);
            }
        }
        return new GetEndpointsResponse(responseHeader, endpointDescriptions);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.MSG;
    }

    public ResponseHeader getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(ResponseHeader responseHeader) {
        this.responseHeader = responseHeader;
    }

    public EndpointDescription[] getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(EndpointDescription[] endpoints) {
        this.endpoints = endpoints;
    }
}
