import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SecretsManager {

    private final String KEYSTORE_FILENAME = "keystore.pkcs12";
    private final String KEYSTORE_PASSWORD = "crypto";
    private KeyPair CAKeyPair;

    public SecretsManager() {
        try {
            this.CAKeyPair = load(KEYSTORE_PASSWORD, KEYSTORE_FILENAME.toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableEntryException e) {
            e.printStackTrace();
        }
    }

    public PublicKey getPublicKey() {
        return CAKeyPair.getPublic();
    }

    public X509Certificate generateCertificate(String username, PublicKey publicKey){
        return CertificateGenerator.generate(username, publicKey, CAKeyPair.getPrivate());
    }

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
