import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * <code>CertificateAuthority</code> is a class that generates a {@link KeyPair}
 * and stores the {@link KeyStore.Entry} in a PKCS12 {@link KeyStore} that is
 * written to disk. Generation must occur in an offline step
 * prior to <code>Client</code>s initiating a connection request to the server.
 *
 * @author Insaaf Dhansay
 * @author Kialan Pillay
 * @author Aidan Bailey
 * @author Emily Morris
 * @version %I%, %G%
 * @see CertificateGenerator
 * @see SecretsManager
 */
public class CertificateAuthority {

    /**
     * Generates a key pair for secure storage.
     */
    public static void main(String[] args) throws Exception {
        KeyPair CAKeyPair = KeyUtils.generate();
        String filename = "keystore.pkcs12";
        char[] password = "crypto".toCharArray();

        store(filename, password, CAKeyPair);
    }

    /**
     * Generates a self-signed certificate using its own Public-Private key pair.
     * Loads PKCS12 keystore and sets an entry to store a private key
     * with a trusted certificate. Writes the keystore to disk.
     */
    private static void store(
            String filename, char[] password,
            KeyPair generatedKeyPair) throws KeyStoreException, IOException,
            NoSuchAlgorithmException, CertificateException {

        X509Certificate rootCertificate = CertificateGenerator.generate("CA",
                generatedKeyPair.getPublic(), generatedKeyPair.getPrivate());

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);

        KeyStore.Entry entry = new KeyStore.PrivateKeyEntry(generatedKeyPair.getPrivate(),
                new X509Certificate[]{rootCertificate});
        KeyStore.ProtectionParameter param = new KeyStore.PasswordProtection(password);

        keyStore.setEntry("CA", entry, param);

        try (FileOutputStream fileOutputStream = new FileOutputStream(filename)) {
            keyStore.store(fileOutputStream, password);
        }
    }

}
