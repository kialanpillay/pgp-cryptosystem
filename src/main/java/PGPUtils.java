import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * <code>PGPUtils</code> is a concrete wrapper class that provides Pretty Good
 * Protocol (PGP) encoding and decoding functionality for a {@link Message}
 * using the {@link java.security.MessageDigest} for hashing,
 * {@link java.util.zip} for compression and {@link javax.crypto.Cipher} for
 * encryption/decription.
 *
 * @author Aidan Bailey, Emily Morris
 * @version %I%, %G%
 */
public class PGPUtils {

    /**
     * Byte length of specified RSA keys (e.g., 128, 256)
     */
    static final int RSAByteLength = 128;

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
     * @param key          specified RSA key
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
     * @param key          specified AES key
     * @param iv           initialization vector
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
            final SecretKey sessionKey, final IvParameterSpec iv)
            throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException {
        final String messageConcat = message.toString();
        final byte[] captionLengthBytes = ByteBuffer.allocate(4).putInt(message.getCaption().length()).array();
        final byte[] messageBytes = concatBytes(captionLengthBytes, messageConcat.getBytes());
        final byte[] signatureBytes = RSAEncryption(SHA256Hash(messageConcat.getBytes()), senderKey);
        final byte[] signedMessage = concatBytes(signatureBytes, messageBytes);
        final byte[] compressedSignedMessage = ZIPCompress(signedMessage);
        final byte[] encryptedSignedMessage = AESEncryption(compressedSignedMessage, sessionKey, iv);
        final byte[] concatSessionData = concatBytes(iv.getIV(), sessionKey.getEncoded());
        final byte[] encryptedSessionData = RSAEncryption(concatSessionData, receiverKey);
        final byte[] pgpMessage = concatBytes(encryptedSessionData, encryptedSignedMessage);
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
    public static Message PGPDecode(final byte[] pgpMessage, final PrivateKey receiverKey, final PublicKey senderKey)
            throws IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException,
            InvalidAlgorithmParameterException, DataFormatException, SignatureException, KeyException {
        // aquire encrypted session data
        final byte[] encryptedSessionData = Arrays.copyOfRange(pgpMessage, 0, RSAByteLength);
        // decrypt session data
        final byte[] sessionData = RSADecryption(encryptedSessionData, receiverKey);
        // acquire IV
        final byte[] ivBytes = Arrays.copyOfRange(sessionData, 0, 16);
        final IvParameterSpec iv = new IvParameterSpec(ivBytes);
        // acquire aes session key
        final byte[] sessionKeyBytes = Arrays.copyOfRange(sessionData, 16, sessionData.length);
        final SecretKey sessionKey = new SecretKeySpec(sessionKeyBytes, 0, sessionKeyBytes.length, "AES");
        // acquire encrypted compressed message
        final byte[] encryptedCompressedMessage = Arrays.copyOfRange(pgpMessage, RSAByteLength, pgpMessage.length);
        // decrypt compressed message
        final byte[] decryptedCompressedMessage = AESDecryption(encryptedCompressedMessage, sessionKey, iv);
        // decompress message
        final byte[] decompressedMessage = ZIPDecompress(decryptedCompressedMessage);
        // acquire signature
        final byte[] signature = Arrays.copyOfRange(decompressedMessage, 0, RSAByteLength);
        // acquire caption length
        final int captionLength = ByteBuffer
                .wrap(Arrays.copyOfRange(decompressedMessage, RSAByteLength, RSAByteLength + 4)).getInt();
        // acquire message
        final byte[] messageBytes = Arrays.copyOfRange(decompressedMessage, RSAByteLength + 4,
                decompressedMessage.length);
        if (!validateSignature(signature, messageBytes, senderKey)) {
            throw new SignatureException("Invalid signature for given message");
        }
        final String messageString = new String(messageBytes);
        // split message using the caption length into caption and picture strings and
        // return result
        return new Message(messageString.substring(captionLength), messageString.substring(0, captionLength));
    }

}
