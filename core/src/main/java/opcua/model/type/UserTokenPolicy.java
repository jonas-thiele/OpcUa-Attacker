package opcua.model.type;

import opcua.encoding.BinarySerializer;
import opcua.encoding.MessageInputStream;
import opcua.security.SecurityPolicy;

import java.io.IOException;

/**
 * UserTokenPolicy (OPC UA Part 4, p. 160)
 */
public class UserTokenPolicy {

    private String policyId;
    private UserTokenType tokenType;
    private String issuedTokenType;
    private String issuerEndpointUrl;
    private SecurityPolicy securityPolicy;

    public UserTokenPolicy() { }

    public UserTokenPolicy(String policyId, UserTokenType tokenType, String issuedTokenType, String issuerEndpointUrl, SecurityPolicy securityPolicy) {
        this.policyId = policyId;
        this.tokenType = tokenType;
        this.issuedTokenType = issuedTokenType;
        this.issuerEndpointUrl = issuerEndpointUrl;
        this.securityPolicy = securityPolicy;
    }

    public byte[] toBinary() {
        return new BinarySerializer()
                .putString(policyId)
                .putEnumeration(tokenType)
                .putString(issuedTokenType)
                .putString(issuerEndpointUrl)
                .putString(securityPolicy.getUri())
                .get();
    }

    public static UserTokenPolicy constructFromBinary(MessageInputStream stream) throws IOException {
        String policyId = stream.readString();
        UserTokenType userTokenType = stream.readEnumeration(UserTokenType.class);
        String issuedTokenType = stream.readString();
        String issuerEndpointUrl = stream.readString();
        SecurityPolicy securityPolicy = SecurityPolicy.fromUri(stream.readString());
        return new UserTokenPolicy(policyId, userTokenType, issuedTokenType, issuerEndpointUrl, securityPolicy);
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public UserTokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(UserTokenType tokenType) {
        this.tokenType = tokenType;
    }

    public String getIssuedTokenType() {
        return issuedTokenType;
    }

    public void setIssuedTokenType(String issuedTokenType) {
        this.issuedTokenType = issuedTokenType;
    }

    public String getIssuerEndpointUrl() {
        return issuerEndpointUrl;
    }

    public void setIssuerEndpointUrl(String issuerEndpointUrl) {
        this.issuerEndpointUrl = issuerEndpointUrl;
    }

    public SecurityPolicy getSecurityPolicy() {
        return securityPolicy;
    }

    public void setSecurityPolicy(SecurityPolicy securityPolicy) {
        this.securityPolicy = securityPolicy;
    }

    public enum UserTokenType {
        ANONYMOUS,
        USERNAME,
        CERTIFICATE,
        ISSUED_TOKEN
    }
}
