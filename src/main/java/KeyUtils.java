import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.*;

public class KeyUtils {

    private static final String DEFAULT_ALGORITHM = "RSA";
    private static final int DEFAULT_KEY_SIZE = 2048;

    private KeyUtils() {
    }

    public static KeyPair generate(String algorithm, int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
        SecureRandom random = SecureRandom.getInstanceStrong();
        generator.initialize(keySize, random);
        return generator.generateKeyPair();
    }

    public static KeyPair generate(String algorithm) throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
        SecureRandom random = SecureRandom.getInstanceStrong();
        generator.initialize(DEFAULT_KEY_SIZE, random);
        return generator.generateKeyPair();
    }

    public static KeyPair generate() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(DEFAULT_ALGORITHM);
        SecureRandom random = SecureRandom.getInstanceStrong();
        generator.initialize(DEFAULT_KEY_SIZE, random);
        return generator.generateKeyPair();
    }

    public static SecretKey generateSessionKey(int keySize) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(keySize);
        return keyGenerator.generateKey();
    }


}
