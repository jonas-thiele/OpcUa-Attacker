package opcua.security;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
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

public class CertificateUtil {

    public static X509Certificate decodeX509FromDer(byte[] derEncodedCertificate) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(derEncodedCertificate));
    }

    public static byte[] createSha1Thumbprint(X509Certificate certificate) throws CertificateEncodingException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            return digest.digest(certificate.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            //Will not happoen
            throw new Error();
        }
    }

    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String name, String signingAlgorithm)
            throws OperatorCreationException, CertificateException {
        X500Name x500Name = new X500Name(name);
        Date startDate = new Date(System.currentTimeMillis());
        Date endDate = new Date(System.currentTimeMillis() + 31556952000L);     //One year lifetime
        BigInteger serialNumber = BigInteger.ONE;
        ContentSigner contentSigner = new JcaContentSignerBuilder(signingAlgorithm).build(keyPair.getPrivate());
        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(x500Name, serialNumber, startDate, endDate, x500Name, subjectPublicKeyInfo);
        X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);
        return new JcaX509CertificateConverter().getCertificate(certificateHolder);
    }

}
