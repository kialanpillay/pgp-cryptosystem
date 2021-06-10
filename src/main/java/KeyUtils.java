import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
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

    public static SecretKey generateSessionKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }

    public static IvParameterSpec generateIV() {
        byte[] initializationVector = new byte[16];
        new SecureRandom().nextBytes(initializationVector);
        return new IvParameterSpec(initializationVector);
    }

}
