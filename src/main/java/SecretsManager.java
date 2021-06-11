import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * <code>SecretsManager</code> is a concrete class that manages secrets
 * required for secure encrypted communication between clients.
 * <code>SecretsManager</code> loads the CA {@link KeyPair} from a keystore
 * and uses a <code>CertificateGenerator</code> to generated digital certificates.
 * <code>Client</code>s use a <code>SecretsManager</code> to retrieve the public key
 * of the Certificate Authority.
 *
 * @author Insaaf Dhansay
 * @author Kialan Pillay
 * @author Aidan Bailey
 * @author Emily Morris
 * @version %I%, %G%
 * @see CertificateGenerator
 */
public class SecretsManager {

    private final String KEYSTORE_FILENAME = "keystore.pkcs12";
    private final String KEYSTORE_PASSWORD = "crypto";
    private KeyPair CAKeyPair;

    /**
     * Sole class constructor
     */
    public SecretsManager() {
        try {
            this.CAKeyPair = load(KEYSTORE_FILENAME, KEYSTORE_PASSWORD.toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableEntryException e) {
            e.printStackTrace();
        }
    }

    public PublicKey getPublicKey() {
        return CAKeyPair.getPublic();
    }

    /**
     * Generates a signed public key certificate
     *
     * @param alias     alias of the certificate subject
     * @param publicKey public key of a client
     * @return <code>X509Certificate</code>
     */
    public X509Certificate generateCertificate(String alias, PublicKey publicKey) {
        return CertificateGenerator.generate(alias, publicKey, CAKeyPair.getPrivate());
    }

    /**
     * Retrieves a <code>PrivateKeyEntry</code> from the keystore on disk
     *
     * @param filename keystore filename
     * @param password keystore password
     * @return <code>X509Certificate</code>
     */
    private KeyPair load(String filename, char[] password)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            IOException, UnrecoverableEntryException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        try (FileInputStream fis = new FileInputStream(filename)) {
            keyStore.load(fis, password);
        }

        KeyStore.ProtectionParameter param = new KeyStore.PasswordProtection(password);
        KeyStore.Entry entry = keyStore.getEntry("CA", param);

        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) entry;
        PublicKey CAPublicKey = privateKeyEntry.getCertificate().getPublicKey();
        PrivateKey CAPrivateKey = privateKeyEntry.getPrivateKey();
        return new KeyPair(CAPublicKey, CAPrivateKey);
    }

}
