package opcua.security;

public enum SecurityPolicy {
    NONE("http://opcfoundation.org/UA/SecurityPolicy#None",
            null,
            null,
            null,
            null,
            null,
            null,
            0,0,0,0,
            0,0,0),

    BASIC128_RSA15("http://opcfoundation.org/UA/SecurityPolicy#Basic128Rsa15",
            SecurityAlgorithm.AES128_CBC,
            SecurityAlgorithm.HMAC_SHA1,
            SecurityAlgorithm.RSA_15,
            SecurityAlgorithm.RSA_SHA1,
            SecurityAlgorithm.KW_RSA_15,
            SecurityAlgorithm.P_SHA1,
            20, 16, 16, 16,
            1024, 2048, 16),

    BASIC256("http://opcfoundation.org/UA/SecurityPolicy#Basic256",
            SecurityAlgorithm.AES256_CBC,
            SecurityAlgorithm.HMAC_SHA1,
            SecurityAlgorithm.RSA_OAEP,
            SecurityAlgorithm.RSA_SHA1,
            SecurityAlgorithm.KW_RSA_OAEP,
            SecurityAlgorithm.P_SHA1,
            20, 24, 32, 16,
            1024, 2048, 32),

    BASIC256_SHA256("http://opcfoundation.org/UA/SecurityPolicy#Basic256Sha256",
            SecurityAlgorithm.AES256_CBC,
            SecurityAlgorithm.HMAC_SHA256,
            SecurityAlgorithm.RSA_OAEP,
            SecurityAlgorithm.RSA_SHA256,
            SecurityAlgorithm.KW_RSA_OAEP,
            SecurityAlgorithm.P_SHA256,
            32, 32, 32, 16,
            2048, 4096, 32),

    AES128_SHA256_RSAOAEP("http://opcfoundation.org/UA/SecurityPolicy#Aes128_Sha256_RsaOaep",
            SecurityAlgorithm.AES128_CBC,
            SecurityAlgorithm.HMAC_SHA256,
            SecurityAlgorithm.RSA_OAEP,
            SecurityAlgorithm.P_SHA256,
            SecurityAlgorithm.KW_RSA_OAEP,
            SecurityAlgorithm.P_SHA256,
            32, 32, 16, 16,
            2048, 4096, 32);

    //TODO AES256_SHA256_RSAPSS

    private final String uri;
    private final byte[] uriBytes;
    private final SecurityAlgorithm symmetricEncryption;
    private final SecurityAlgorithm symmetricSignature;
    private final SecurityAlgorithm asymmetricEncryption;
    private final SecurityAlgorithm asymmetricSignature;
    private final SecurityAlgorithm asymmetricKeywrap;
    private final SecurityAlgorithm keyDerivation;
    //security parameters
    private final int hmacHashSize;
    private final int signatureKeySize;
    private final int encryptionKeySize;
    private final int encryptionBlockSize;
    private final int minAsymmetricKeyLength;
    private final int maxAsymmetricKeyLength;
    private final int symmetricEncryptionNonceLength;

    SecurityPolicy(String uri, SecurityAlgorithm symmetricEncryption, SecurityAlgorithm symmetricSignature,
                   SecurityAlgorithm asymmetricEncryption, SecurityAlgorithm asymmetricSignature,
                   SecurityAlgorithm asymmetricKeywrap, SecurityAlgorithm keyDerivation, int hmacHashSize,
                   int signatureKeySize, int encryptionKeySize, int encryptionBlockSize, int minAsymmetricKeyLength,
                   int maxAsymmetricKeyLength, int symmetricEncryptionNonceLength)
    {
        this.uri = uri;
        this.uriBytes = uri.getBytes();
        this.symmetricEncryption = symmetricEncryption;
        this.symmetricSignature = symmetricSignature;
        this.asymmetricEncryption = asymmetricEncryption;
        this.asymmetricSignature = asymmetricSignature;
        this.asymmetricKeywrap = asymmetricKeywrap;
        this.keyDerivation = keyDerivation;
        this.hmacHashSize = hmacHashSize;
        this.signatureKeySize = signatureKeySize;
        this.encryptionKeySize = encryptionKeySize;
        this.encryptionBlockSize = encryptionBlockSize;
        this.minAsymmetricKeyLength = minAsymmetricKeyLength;
        this.maxAsymmetricKeyLength = maxAsymmetricKeyLength;
        this.symmetricEncryptionNonceLength = symmetricEncryptionNonceLength;
    }

    public static SecurityPolicy fromUri(String uri) {
        if(uri == null) {
            return SecurityPolicy.NONE;
        }
        for(SecurityPolicy p : SecurityPolicy.values()) {
            if(p.getUri().equals(uri)) {
                return p;
            }
        }
        throw new Error("TODO");
    }

    public String getUri() {
        return uri;
    }

    public byte[] getUriBytes() {
        return uriBytes;
    }

    public SecurityAlgorithm getSymmetricEncryption() {
        return symmetricEncryption;
    }

    public SecurityAlgorithm getSymmetricSignature() {
        return symmetricSignature;
    }

    public SecurityAlgorithm getAsymmetricEncryption() {
        return asymmetricEncryption;
    }

    public SecurityAlgorithm getAsymmetricSignature() {
        return asymmetricSignature;
    }

    public SecurityAlgorithm getAsymmetricKeywrap() {
        return asymmetricKeywrap;
    }

    public SecurityAlgorithm getKeyDerivation() {
        return keyDerivation;
    }

    public int getHmacHashSize() {
        return hmacHashSize;
    }

    public int getSignatureKeySize() {
        return signatureKeySize;
    }

    public int getEncryptionKeySize() {
        return encryptionKeySize;
    }

    public int getEncryptionBlockSize() {
        return encryptionBlockSize;
    }

    public int getMinAsymmetricKeyLength() {
        return minAsymmetricKeyLength;
    }

    public int getMaxAsymmetricKeyLength() {
        return maxAsymmetricKeyLength;
    }

    public int getSymmetricEncryptionNonceLength() {
        return symmetricEncryptionNonceLength;
    }


}
