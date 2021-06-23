import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * <code>PGPUtils</code> is a concrete wrapper class that provides Pretty Good
 * Protocol (PGP) encoding and decoding functionality for a {@link Message}
 * using the {@link java.security.MessageDigest} for hashing,
 * {@link java.util.zip} for compression and {@link javax.crypto.Cipher} for
 * encryption/decryption.
 *
 * @author Aidan Bailey
 * @author Emily Morris
 * @author Insaaf Dhansay
 * @author Kialan Pillay
 * @version %I%, %G%
 */
public class PGPUtils {

    /**
     * Byte length of specified RSA keys (e.g., 128, 256)
     */
    private static final int RSA_BYTE_LENGTH = 128;

    /**
     * Sole class constructor
     */
    private PGPUtils() {
    }

    /**
     * SHA-256 hashes a byte-array using the {@link MessageDigest}.
     *
     * @param messageBytes byte array of the message to be hashed
     * @return <code>byte[]</code>
     */
    private static byte[] SHA256Hash(final byte[] messageBytes) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(messageBytes);
    }

    /**
     * ZIP compresses a byte-array using the {@link Deflater}.
     *
     * @param inputBytes bytes to be compressed
     * @return <code>byte[]</code>
     */
    private static byte[] ZIPCompress(final byte[] inputBytes) {
        final Deflater deflater = new Deflater();
        deflater.setInput(inputBytes);
        deflater.finish();
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final byte[] readBuffer = new byte[1024];
        int readCount;
        while (!deflater.finished()) {
            readCount = deflater.deflate(readBuffer);
            if (readCount > 0) {
                byteArrayOutputStream.write(readBuffer, 0, readCount);
            }
        }
        deflater.end();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Decompresses a ZIP compressed byte-array using the {@link Inflater}.
     *
     * @param inputBytes bytes to be uncompressed
     * @return <code>byte[]</code>
     */
    private static byte[] ZIPDecompress(final byte[] inputBytes) throws DataFormatException {
        final Inflater inflater = new Inflater();
        inflater.setInput(inputBytes);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final byte[] readBuffer = new byte[1024];
        int readCount;
        while (!inflater.finished()) {
            readCount = inflater.inflate(readBuffer);
            if (readCount > 0) {
                byteArrayOutputStream.write(readBuffer, 0, readCount);
            }
        }
        inflater.end();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * RSA encrypts a byte-array with a specified key using the {@link Cipher}.
     *
     * @param messageBytes bytes of message to be encrypted
     * @param key          specified RSA key
     * @return <code>byte[]</code>
     */
    private static byte[] RSAEncryption(final byte[] messageBytes, final Key key) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        final Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key);
        return encryptCipher.doFinal(messageBytes);
    }

    /**
     * Decrypts an RSA encrypted byte-array with a specified key using the
     * {@link Cipher}.
     *
     * @param encryptedMessageBytes bytes of message to be decrypted
     * @param key                   specified RSA key
     * @return <code>byte[]</code>
     */
    private static byte[] RSADecryption(final byte[] encryptedMessageBytes, final Key key) throws InvalidKeyException,
            NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {
        final Cipher decryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        decryptCipher.init(Cipher.DECRYPT_MODE, key);
        return decryptCipher.doFinal(encryptedMessageBytes);
    }

    /**
     * AES encrypts a byte-array with a specified key and initialization vector
     * using the {@link Cipher}.
     *
     * @param messageBytes bytes of message to be encrypted
     * @param key          specified AES key
     * @param iv           initialization vector
     * @return <code>byte[]</code>
     */
    private static byte[] AESEncryption(final byte[] messageBytes, final Key key, final IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        final Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key, iv);
        return encryptCipher.doFinal(messageBytes);
    }

    /**
     * Decrypts an AES encrypted byte-array with a specified key and initialization
     * vector using the {@link Cipher}.
     *
     * @param encryptedMessageBytes bytes of message to be decrypted
     * @param key                   specified AES key
     * @param iv                    initialization vector
     * @return <code>byte[]</code>
     */
    private static byte[] AESDecryption(final byte[] encryptedMessageBytes, final Key key, final IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        final Cipher decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decryptCipher.init(Cipher.DECRYPT_MODE, key, iv);
        return decryptCipher.doFinal(encryptedMessageBytes);
    }

    /**
     * Concatenates two byte-arrays.
     *
     * @param prefBytes bytes to be on the front
     * @param sufBytes  bytes to be on the back
     * @return <code>byte[]</code>
     */
    private static byte[] concatBytes(final byte[] prefBytes, final byte[] sufBytes) {
        final byte[] concatBytes = new byte[prefBytes.length + sufBytes.length];
        System.arraycopy(prefBytes, 0, concatBytes, 0, prefBytes.length);
        System.arraycopy(sufBytes, 0, concatBytes, prefBytes.length, sufBytes.length);
        return concatBytes;
    }

    /**
     * Validates a digital signature against a specified message and public key.
     *
     * @param signature    bytes of the signature to be validated
     * @param messageBytes bytes of message to be validated against
     * @param key          specified public key
     * @return <code>boolean</code>
     */
    private static boolean validateSignature(final byte[] signature, final byte[] messageBytes, final PublicKey key)
            throws IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException,
            InvalidKeyException {
        final byte[] decryptedSignature = RSADecryption(signature, key);
        final byte[] hashedMessage = SHA256Hash(messageBytes);
        return Arrays.equals(decryptedSignature, hashedMessage);
    }

    /**
     * Encodes a message to be sent using PGP.
     *
     * @param message     message to be encoded
     * @param senderKey   private key of sender
     * @param receiverKey public key of receiver
     * @return <code>byte[]</code>
     */
    public static byte[] PGPEncode(final Message message, final PrivateKey senderKey, final PublicKey receiverKey,
                                   final Logger logger)
            throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException {
        final SecretKey sessionKey = KeyUtils.generateSessionKey();
        logger.info("Session key: " + sessionKey.getEncoded().toString());
        logger.info("Session key algorithm: " + sessionKey.getAlgorithm());
        final IvParameterSpec iv = KeyUtils.generateIV();
        final String messageConcat = message.toString();
        final byte[] captionLengthBytes = ByteBuffer.allocate(4).putInt(message.getCaption().length()).array();
        final byte[] messageBytes = concatBytes(captionLengthBytes, messageConcat.getBytes());
        logger.info("Hashed message: " + SHA256Hash(messageConcat.getBytes()).toString());
        final byte[] signatureBytes = RSAEncryption(SHA256Hash(messageConcat.getBytes()), senderKey);
        final byte[] signedMessage = concatBytes(signatureBytes, messageBytes);
        logger.info("Signed message length: " + signedMessage.length);
        final byte[] compressedSignedMessage = ZIPCompress(signedMessage);
        logger.info("Compressed signed message length: " + compressedSignedMessage.length);
        final byte[] encryptedSignedMessage = AESEncryption(compressedSignedMessage, sessionKey, iv);
        final byte[] concatSessionData = concatBytes(iv.getIV(), sessionKey.getEncoded());
        final byte[] encryptedSessionData = RSAEncryption(concatSessionData, receiverKey);
        final byte[] pgpMessage = concatBytes(encryptedSessionData, encryptedSignedMessage);
        logger.info("Encrypted message bytes: " + pgpMessage.toString());
        return pgpMessage;
    }

    /**
     * Encodes a message to be sent using PGP.
     *
     * @param pgpMessage  message to be encoded
     * @param senderKey   private key of receiver
     * @param receiverKey public key of sender
     * @return <code>Message</code>
     */
    public static Message PGPDecode(final byte[] pgpMessage, final PrivateKey receiverKey, final PublicKey senderKey,
                                    final Logger logger)
            throws IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException,
            InvalidAlgorithmParameterException, DataFormatException, SignatureException, KeyException {
        // acquire encrypted session data
        final byte[] encryptedSessionData = Arrays.copyOfRange(pgpMessage, 0, RSA_BYTE_LENGTH);
        // decrypt session data
        final byte[] sessionData = RSADecryption(encryptedSessionData, receiverKey);
        // acquire IV
        final byte[] ivBytes = Arrays.copyOfRange(sessionData, 0, 16);
        final IvParameterSpec iv = new IvParameterSpec(ivBytes);
        // acquire aes session key
        final byte[] sessionKeyBytes = Arrays.copyOfRange(sessionData, 16, sessionData.length);
        final SecretKey sessionKey = new SecretKeySpec(sessionKeyBytes, 0, sessionKeyBytes.length, "AES");
        logger.info("Session key: " + sessionKey.getEncoded().toString());
        logger.info("Session key algorithm: " + sessionKey.getAlgorithm());
        // acquire encrypted compressed message
        final byte[] encryptedCompressedMessage = Arrays.copyOfRange(pgpMessage, RSA_BYTE_LENGTH, pgpMessage.length);
        // decrypt compressed message
        final byte[] decryptedCompressedMessage = AESDecryption(encryptedCompressedMessage, sessionKey, iv);
        // decompress message
        logger.info("Compressed message length: " + decryptedCompressedMessage.length);
        final byte[] decompressedMessage = ZIPDecompress(decryptedCompressedMessage);
        logger.info("Decompressed message length: " + decompressedMessage.length);
        // acquire signature
        final byte[] signature = Arrays.copyOfRange(decompressedMessage, 0, RSA_BYTE_LENGTH);
        // acquire caption length
        final int captionLength = ByteBuffer
                .wrap(Arrays.copyOfRange(decompressedMessage, RSA_BYTE_LENGTH, RSA_BYTE_LENGTH + 4)).getInt();
        // acquire message
        final byte[] messageBytes = Arrays.copyOfRange(decompressedMessage, RSA_BYTE_LENGTH + 4,
                decompressedMessage.length);
        logger.info("Hashed message: " + SHA256Hash(messageBytes).toString());
        if (!validateSignature(signature, messageBytes, senderKey)) {
            throw new SignatureException("Invalid signature for given message");
        }
        logger.info("Message signature validated");
        final String messageString = new String(messageBytes);
        // split message using the caption length into caption and picture strings and
        // return result
        logger.info("Decrypted message caption: " + messageString.substring(0, captionLength));
        return new Message(messageString.substring(captionLength), messageString.substring(0, captionLength));
    }

}
