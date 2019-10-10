package opcua.security;

/**
 * Message Security Mode (OPC UA Part 4, p.130)
 */
public enum MessageSecurityMode {
    INVALID,
    NONE,
    SIGN,
    SIGN_AND_ENCRYPT
}
