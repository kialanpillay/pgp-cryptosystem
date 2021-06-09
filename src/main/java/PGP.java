import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.zip.*;

public class PGP {

    public static byte[] RSAEncryption(byte[] messageBytes, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(messageBytes);
        return encryptedMessageBytes;
    }

    public static byte[] RSADecryption(byte[] encryptedMessageBytes, Key key) throws InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {
        Cipher decryptCipher = Cipher.getInstance("RSA");
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

    public static IvParameterSpec generateIV() {
        byte[] initializationVector = new byte[16];
        new SecureRandom().nextBytes(initializationVector);
        return new IvParameterSpec(initializationVector);
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

}