import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.zip.*;

public class PGP {

    public static byte [] hash(byte[] message) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(message);
    }

    public static byte[] RSAEncryption(byte[] messageBytes, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(messageBytes);
        System.out.println(encryptedMessageBytes.length);
        return encryptedMessageBytes;
    }

    public static byte[] RSADecryption(byte[] encryptedMessageBytes, Key key) throws InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {
        Cipher decryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        decryptCipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);
        return decryptedMessageBytes;
    }

    public static byte[] AESEncryption(byte[] messageBytes, Key key, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encryptedMessageBytes = cipher.doFinal(messageBytes);
        return encryptedMessageBytes;
    }

    public static byte[] AESDecryption(byte[] encryptedMessageBytes, Key key, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] decryptedMessageBytes = cipher.doFinal(encryptedMessageBytes);
        return decryptedMessageBytes;
    }

    public static byte[] compress(byte[] message) {
        Deflater compressor = new Deflater();
        compressor.setInput(message);
        compressor.finish();
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        byte[] readBuffer = new byte[1024];
        int readCount = 0;
        while (!compressor.finished()) {
            readCount = compressor.deflate(readBuffer);
            if (readCount > 0) {
                bao.write(readBuffer, 0, readCount);
            }
        }
        compressor.end();
        return bao.toByteArray();
    }

    public static byte[] decompress(byte[] input) throws DataFormatException {
        Inflater decompressor = new Inflater();
        decompressor.setInput(input);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        byte[] readBuffer = new byte[1024];
        int readCount = 0;
        while (!decompressor.finished()) {
            readCount = decompressor.inflate(readBuffer);
            if (readCount > 0) {
                bao.write(readBuffer, 0, readCount);
            }
        }
        decompressor.end();
        return bao.toByteArray();
    }

    public static byte[] PGPEncode (Message message, PrivateKey senderKey, PublicKey receiverKey, SecretKey sessionKey, IvParameterSpec iv) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        String messageConcat = message.toString();
        byte [] captionLengthBytes = ByteBuffer.allocate(4).putInt(message.getCaption().length()).array();
        byte [] messageBytes = concatBytes(captionLengthBytes,messageConcat.getBytes()); // length of caption added to front of message
        byte [] signatureBytes = RSAEncryption(hash(messageConcat.getBytes()),senderKey);
        byte [] signedMessage = concatBytes(signatureBytes, messageBytes); // Signature first, then message
        byte [] compressedSignedMessage = compress(signedMessage);
        byte [] encryptedSignedMessage = AESEncryption(compressedSignedMessage, sessionKey, iv);
        byte [] concatSessionData = concatBytes(iv.getIV(), sessionKey.getEncoded());
        byte [] encryptedSessionData = RSAEncryption(concatSessionData, receiverKey);
        byte [] pgpMessage = concatBytes(encryptedSessionData, encryptedSignedMessage);
        return pgpMessage;
    }

    public static Message PGPDecode (byte [] pgpMessage, PrivateKey receiverKey, PublicKey senderKey) throws IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, DataFormatException, SignatureException {
        // encrypted session data
        byte [] encryptedSessionData = Arrays.copyOfRange(pgpMessage, 0, 128);
        // decrypted session data
        byte [] concatSessionData = RSADecryption(encryptedSessionData,  receiverKey);
        // acquire IV
        byte [] ivBytes = Arrays.copyOfRange(concatSessionData, 0, 16);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        // acquire session key for aes
        byte[] sessionKeyBytes = Arrays.copyOfRange(concatSessionData, 16, concatSessionData.length);
        SecretKey sessionKey = new SecretKeySpec(sessionKeyBytes, 0, sessionKeyBytes.length, "AES");
        // acquire encrypted compressed message
        byte [] encryptedCompressedMessage = Arrays.copyOfRange(pgpMessage, 128, pgpMessage.length);
        // decrypt compressed message
        byte [] decryptedCompressedMessage = AESDecryption(encryptedCompressedMessage, sessionKey, iv);
        // decompress decrypted message
        byte [] decompressedMessage = decompress(decryptedCompressedMessage);
        // acquire signature
        byte [] signature = Arrays.copyOfRange(decompressedMessage, 0, 128);
        // acquire caption length
        int captionLength = ByteBuffer.wrap( Arrays.copyOfRange(decompressedMessage, 128, 132)).getInt();
        // acquire message
        byte [] message = Arrays.copyOfRange(decompressedMessage, 132, decompressedMessage.length);
        if (!isValid(signature, message, senderKey)){
            throw new SignatureException("Invalid signature for given message");
        }
        String messageString = new String (message);
        // split message into caption and picture using caption length
        Message result = new Message(messageString.substring(captionLength),messageString.substring(0,captionLength));
        return result;
    }

    private static boolean isValid(byte[] signature, byte [] message, PublicKey key) throws IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte [] decryptedSignature = RSADecryption(signature, key);
        byte [] hashedMessage = hash(message);
        return Arrays.equals(decryptedSignature, hashedMessage);
    }

    private static byte[] concatBytes (byte [] prefBytes, byte [] sufBytes){
        byte[] result = new byte[prefBytes.length + sufBytes.length];
        System.arraycopy(prefBytes, 0, result, 0, prefBytes.length);
        System.arraycopy(sufBytes, 0, result, prefBytes.length, sufBytes.length);
        return result;
    }
}