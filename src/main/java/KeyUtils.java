import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * <code>KeyGenerator</code> is a concrete wrapper class that generates
 * key-pairs using the {@link java.security.KeyPairGenerator}
 *
 * @author Kialan Pillay
 * @version %I%, %G%
 */
public class KeyUtils {

    private static final String DEFAULT_ALGORITHM = "RSA";
    private static final int DEFAULT_KEY_SIZE = 2048;

    /**
     * Sole class constructor
     */
    private KeyUtils() {
    }

    /**
     * Generates a key-pair of the specified size using a cryptographic algorithm
     *
     * @param algorithm algorithm to generate the key-pair
     * @param keySize   bit-size of the generated key
     * @return <code>KeyPair</code>
     */
    public static KeyPair generate(String algorithm, int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
        SecureRandom random = SecureRandom.getInstanceStrong();
        generator.initialize(keySize, random);
        return generator.generateKeyPair();
    }

    /**
     * Generates a 2048-bit key-pair using the specified cryptographic algorithm
     *
     * @param algorithm algorithm to generate the key-pair
     * @return <code>KeyPair</code>
     */
    public static KeyPair generate(String algorithm) throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
        SecureRandom random = SecureRandom.getInstanceStrong();
        generator.initialize(DEFAULT_KEY_SIZE, random);
        return generator.generateKeyPair();
    }

    /**
     * Generates a 2048-bit key-pair using the RSA algorithm
     *
     * @return <code>KeyPair</code>
     */
    public static KeyPair generate() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(DEFAULT_ALGORITHM);
        SecureRandom random = SecureRandom.getInstanceStrong();
        generator.initialize(DEFAULT_KEY_SIZE, random);
        return generator.generateKeyPair();
    }
}
