package opcua.security;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.cert.*;
import java.util.Date;

/**
 * Provides utility functions for certificate management
 */
public class CertificateUtility {

    /**
     * Creates decodes a der-encoded certificate
     * @param derEncodedCertificate encoded bytes
     * @return Decoded certificate
     * @throws CertificateException
     */
    public static X509Certificate decodeX509FromDer(byte[] derEncodedCertificate) throws CertificateException {
        if(derEncodedCertificate == null || derEncodedCertificate.length == 0) {
            return null;
        }
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(derEncodedCertificate));
    }

    /**
     * Creates SHA1 thumbprint for a certificate
     * @param certificate The certificate
     * @return SHA1 thumbprint
     * @throws CertificateEncodingException
     */
    public static byte[] createSha1Thumbprint(X509Certificate certificate) throws CertificateEncodingException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            return digest.digest(certificate.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            //Will not happoen
            throw new Error();
        }
    }

    /**
     * Generates self-signed X509 certificate
     * @param keyPair Key pair for certificate
     * @param name Certificate name
     * @param signingAlgorithm Algorithm for signing the certificate
     * @return Self-signed certificate
     * @throws OperatorCreationException
     * @throws CertificateException
     */
    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String name, String signingAlgorithm)
            throws OperatorCreationException, CertificateException {
        X500Name x500Name = new X500Name(name);
        Date startDate = new Date(System.currentTimeMillis());
        Date endDate = new Date(System.currentTimeMillis() + 31556952000L);     //One year lifetime
        BigInteger serialNumber = BigInteger.ONE;
        ContentSigner contentSigner = new JcaContentSignerBuilder(signingAlgorithm).build(keyPair.getPrivate());
        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
        X509v1CertificateBuilder certificateBuilder = new X509v1CertificateBuilder(x500Name, serialNumber, startDate, endDate, x500Name, subjectPublicKeyInfo);
        X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);
        return new JcaX509CertificateConverter().getCertificate(certificateHolder);
    }

}
