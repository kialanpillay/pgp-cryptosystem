import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * <code>KeyUtils</code> is a concrete wrapper class that generates key-pairs
 * using the {@link java.security.KeyPairGenerator}, keys using the
 * {@link javax.crypto.KeyGenerator} and initialization vectors using the
 * {@link javax.crypto.spec.IvParameterSpec}.
 *
 * @author Aidan Bailey
 * @author Emily Morris
 * @author Kialan Pillay
 * @author Insaaf Dhansay
 * @version %I%, %G%
 */
public class KeyUtils {

    private static final String DEFAULT_ALGORITHM = "RSA";
    private static final int DEFAULT_KEY_SIZE = 1024;

    /**
     * Sole class constructor
     */
    private KeyUtils() {
    }

    /**
     * Generates a key-pair of the specified size using a cryptographic algorithm.
     *
     * @param algorithm algorithm to generate the key-pair
     * @param keySize   bit-size of the generated key
     * @return <code>KeyPair</code>
     */
    public static KeyPair generate(final String algorithm, final int keySize) throws NoSuchAlgorithmException {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
        final SecureRandom random = SecureRandom.getInstanceStrong();
        generator.initialize(keySize, random);
        return generator.generateKeyPair();
    }

    /**
     * Generates a 1024-bit key-pair using the specified cryptographic algorithm.
     *
     * @param algorithm algorithm to generate the key-pair
     * @return <code>KeyPair</code>
     */
    public static KeyPair generate(final String algorithm) throws NoSuchAlgorithmException {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
        final SecureRandom random = SecureRandom.getInstanceStrong();
        generator.initialize(DEFAULT_KEY_SIZE, random);
        return generator.generateKeyPair();
    }

    /**
     * Generates a 2048-bit key-pair using the RSA algorithm.
     *
     * @return <code>KeyPair</code>
     */
    public static KeyPair generate() throws NoSuchAlgorithmException {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance(DEFAULT_ALGORITHM);
        final SecureRandom random = SecureRandom.getInstanceStrong();
        generator.initialize(DEFAULT_KEY_SIZE, random);
        return generator.generateKeyPair();
    }

    /**
     * Generates a 128-bit AES session key.
     *
     * @return <code>SecretKey</code>
     */
    public static SecretKey generateSessionKey() throws NoSuchAlgorithmException {
        final javax.crypto.KeyGenerator keyGenerator = javax.crypto.KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }

    /**
     * Generates a 128-bit initialisation vector.
     *
     * @return <code>IvParameterSpec</code>
     */
    public static IvParameterSpec generateIV() {
        final byte[] initializationVector = new byte[16];
        new SecureRandom().nextBytes(initializationVector);
        return new IvParameterSpec(initializationVector);
    }
}
