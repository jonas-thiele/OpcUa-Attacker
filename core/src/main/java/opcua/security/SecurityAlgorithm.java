package opcua.security;

/**
 * Security algorithms that are used in the OPC UA specification
 */
public enum SecurityAlgorithm {
    AES128_CBC("http://www.w3.org/2001/04/xmlenc#aes128-cbc", AlgorithmType.SYMMETRIC_ENCRYPTION, 128),
    AES256_CBC("http://www.w3.org/2001/04/xmlenc#aes256-cbc", AlgorithmType.SYMMETRIC_ENCRYPTION, 256),

    HMAC_SHA1("http://www.w3.org/2000/09/xmldsig#hmac-sha1", AlgorithmType.SYMMETRIC_SIGNATURE, 160),
    HMAC_SHA256("http://www.w3.org/2000/09/xmldsig#hmac-sha256", AlgorithmType.SYMMETRIC_SIGNATURE, 256),

    RSA_15("http://www.w3.org/2001/04/xmlenc#rsa-1_5", AlgorithmType.ASYMMETRIC_ENCRYPTION, 0),
    RSA_OAEP("http://www.w3.org/2001/04/xmlenc#rsa-oaep", AlgorithmType.ASYMMETRIC_ENCRYPTION, 0),

    RSA_SHA1("http://www.w3.org/2000/09/xmldsig#rsa-sha1", AlgorithmType.ASYMMETRIC_SIGNATURE, 160),
    RSA_SHA256("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", AlgorithmType.ASYMMETRIC_SIGNATURE, 256),
    RSA_PSS_SHA256("http://opcfoundation.org/UA/security/rsa-pss-sha2-256", AlgorithmType.ASYMMETRIC_SIGNATURE, 256),

    KW_RSA_15("http://www.w3.org/2001/04/xmlenc#rsa-1_5", AlgorithmType.ASYMMETRIC_KEYWRAP, 0),
    KW_RSA_OAEP("http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p", AlgorithmType.ASYMMETRIC_KEYWRAP, 0),

    P_SHA1("http://www.w3.org/2001/04/xmlenc#aes128-cbc", AlgorithmType.KEY_DERIVATION, 0),
    P_SHA256("http://docs.oasis-open.org/ws-sx/ws-secureconversation/200512/dk/p_sha256", AlgorithmType.KEY_DERIVATION, 0);

    public enum AlgorithmType {
        SYMMETRIC_ENCRYPTION,
        SYMMETRIC_SIGNATURE,
        ASYMMETRIC_ENCRYPTION,
        ASYMMETRIC_SIGNATURE,
        ASYMMETRIC_KEYWRAP,
        KEY_DERIVATION
    }

    private final String uri;
    private final AlgorithmType type;
    private final int keySize;

    SecurityAlgorithm(String uri, AlgorithmType type, int keySize) {
        this.uri = uri;
        this.type = type;
        this.keySize = keySize;
    }

    public String getUri() {
        return uri;
    }

    public AlgorithmType getType() {
        return type;
    }

    public int getKeySize() {
        return keySize;
    }
}
