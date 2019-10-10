package opcua.model.type;

import opcua.encoding.BinarySerializer;
import opcua.encoding.MessageInputStream;

import java.io.IOException;

/**
 * ApplicationDescription (OPC UA Part 4, p.114)
 */
public class ApplicationDescription {
    private String applicationUri;
    private String productUri;
    private String applicationName;
    private ApplicationType applicationType;
    private String gatewayServerUri;
    private String discoveryProfileUri;
    private String[] discoveryUrls;

    public ApplicationDescription() { }

    public ApplicationDescription(String applicationUri, String productUri, String applicationName, ApplicationType applicationType, String gatewayServerUri, String discoveryProfileUri, String[] discoveryUrls) {
        this.applicationUri = applicationUri;
        this.productUri = productUri;
        this.applicationName = applicationName;
        this.applicationType = applicationType;
        this.gatewayServerUri = gatewayServerUri;
        this.discoveryProfileUri = discoveryProfileUri;
        this.discoveryUrls = discoveryUrls;
    }

    public byte[] toBinary() {
        return new BinarySerializer()
                .putString(applicationUri)
                .putString(productUri)
                .putString(applicationName)
                .putEnumeration(applicationType)
                .putString(gatewayServerUri)
                .putString(discoveryProfileUri)
                .putStringArray(discoveryUrls)
                .get();
    }

    public static ApplicationDescription constructFromBinary(MessageInputStream stream) throws IOException {
        String applicationUri = stream.readString();
        String productUri = stream.readString();
        String applicationName = stream.readLocalizedText();
        ApplicationType applicationType = stream.readEnumeration(ApplicationType.class);
        String gatewayServerUri = stream.readString();
        String dicoveryProfileUri = stream.readString();
        String[] discoveryUrls = stream.readStringArray();
        return new ApplicationDescription(applicationUri, productUri, applicationName, applicationType, gatewayServerUri, dicoveryProfileUri, discoveryUrls);
    }

    public String getApplicationUri() {
        return applicationUri;
    }

    public void setApplicationUri(String applicationUri) {
        this.applicationUri = applicationUri;
    }

    public String getProductUri() {
        return productUri;
    }

    public void setProductUri(String productUri) {
        this.productUri = productUri;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public String getGatewayServerUri() {
        return gatewayServerUri;
    }

    public void setGatewayServerUri(String gatewayServerUri) {
        this.gatewayServerUri = gatewayServerUri;
    }

    public String getDiscoveryProfileUri() {
        return discoveryProfileUri;
    }

    public void setDiscoveryProfileUri(String discoveryProfileUri) {
        this.discoveryProfileUri = discoveryProfileUri;
    }

    public String[] getDiscoveryUrls() {
        return discoveryUrls;
    }

    public void setDiscoveryUrls(String[] discoveryUrls) {
        this.discoveryUrls = discoveryUrls;
    }

    public enum ApplicationType {
        SERVER,
        CLIENT,
        CLIENT_AND_SERVER,
        DISCOVERY_SERVER
    }
}
