package opcua.context;

import opcua.security.CertificateUtility;
import org.bouncycastle.operator.OperatorCreationException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Combination of certificate and private key
 */
public class LocalKeyPair {
    public static LocalKeyPair NULL = new LocalKeyPair(null, null);

    private final RSAPrivateKey privateKey;
    private final X509Certificate certificate;

    /**
     * Constructor
     * @param privateKey
     * @param certificate
     */
    public LocalKeyPair(RSAPrivateKey privateKey, X509Certificate certificate) {
        this.privateKey = privateKey;
        this.certificate = certificate;
    }

    /**
     * Returns LocalKeyPair with generated keypair and self-signed certificate
     * @param keySize Key size
     * @param certificateName Name of the certificate
     * @return Generated LocalKeyPair
     */
    public static LocalKeyPair generateSelfSigned(int keySize, String certificateName) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keySize);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPrivateKey privateKey = (RSAPrivateKey)keyPair.getPrivate();
            X509Certificate cert = CertificateUtility.generateSelfSignedCertificate(keyPair, certificateName, "SHA256withRSA");
            return new LocalKeyPair(privateKey, cert);
        } catch (NoSuchAlgorithmException | OperatorCreationException | CertificateException e) {
            throw new Error(e);
        }
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public RSAPublicKey getPublicKey() {
        if(certificate == null) {
            return null;
        }
        return (RSAPublicKey)certificate.getPublicKey();
    }

    public X509Certificate getCertificate() {
        return certificate;
    }


}
