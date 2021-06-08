import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class KeyGenerator {

    private final KeyPairGenerator generator;

    public KeyGenerator() throws NoSuchAlgorithmException {
        generator = KeyPairGenerator.getInstance("RSA");

    }

    public KeyGenerator(String algorithm) throws NoSuchAlgorithmException {
        generator = KeyPairGenerator.getInstance(algorithm);
    }

    public KeyPair generate(int keySize) throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        generator.initialize(keySize, random);
        return generator.generateKeyPair();
    }
}
