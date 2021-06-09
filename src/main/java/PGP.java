import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class PGP {
    public static String hash(String message) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(
                message.getBytes(StandardCharsets.UTF_8));
        return new String(encodedhash);
    }

    public static String RSAEncryption(String message, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] secretMessageBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
        return new String(encryptedMessageBytes);
    }
}
