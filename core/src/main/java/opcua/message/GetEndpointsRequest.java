package opcua.message;

import opcua.context.StaticConfig;
import opcua.message.parts.MessageType;
import opcua.message.parts.RequestHeader;
import opcua.encoding.BinarySerializer;
import opcua.model.type.ObjectIds;

/**
 * GetEndpoints Request (OPC UA Part 4, p. 15)
 */
public class GetEndpointsRequest extends SecureConversationMessage {
    public static RequestHeader DEFAULT_REQUEST_HEADER = new RequestHeader(ObjectIds.GET_ENDPOINTS_REQUEST, null, 0, 0, "", StaticConfig.TIMEOUT);

    private RequestHeader requestHeader;
    private String endpointUrl;
    private String[] localIds;
    private String[] profileIds;

    public GetEndpointsRequest(RequestHeader requestHeader, String endpointUrl, String[] localIds, String[] profileIds) {
        this.requestHeader = requestHeader;
        this.endpointUrl = endpointUrl;
        this.localIds = localIds;
        this.profileIds = profileIds;
    }

    public GetEndpointsRequest(String endpointUrl, String[] localIds, String[] profileIds) {
        this.requestHeader = DEFAULT_REQUEST_HEADER;
        this.endpointUrl = endpointUrl;
        this.localIds = localIds;
        this.profileIds = profileIds;
    }

    public GetEndpointsRequest() { }

    @Override
    public byte[] toBinary() {
        return new BinarySerializer()
                .putBytes(requestHeader.toBinary())
                .putString(endpointUrl)
                .putStringArray(localIds)
                .putStringArray(profileIds)
                .get();
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.MSG;
    }

    public RequestHeader getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(RequestHeader requestHeader) {
        this.requestHeader = requestHeader;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public String[] getLocalIds() {
        return localIds;
    }

    public void setLocalIds(String[] localIds) {
        this.localIds = localIds;
    }

    public String[] getProfileIds() {
        return profileIds;
    }

    public void setProfileIds(String[] profileIds) {
        this.profileIds = profileIds;
    }
}
