import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CertificateAuthority {

    public static void main(String[] args) throws Exception {
        KeyPair CAKeyPair = KeyUtils.generate();
        String filename = "keystore.pkcs12";
        char[] password = "crypto".toCharArray();

        store(filename, password, CAKeyPair);
    }

    private static X509Certificate generateSelfSignedCertificate(KeyPair keyPair) {

        X500Name issuer = new X500Name("cn=CA");
        X500Name subject = new X500Name("cn=CA");
        Date before = new Date();
        Date after = new GregorianCalendar(2021, Calendar.DECEMBER, 31).getTime();
        BigInteger sn = new BigInteger(64, new SecureRandom());

        X509v3CertificateBuilder v3CertificateBuilder = new JcaX509v3CertificateBuilder(issuer, sn, before, after, subject, keyPair.getPublic());
        X509Certificate certificate = null;
        try {
            ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(new BouncyCastleProvider()).build(keyPair.getPrivate());
            X509CertificateHolder certificateHolder = v3CertificateBuilder.build(signer);
            certificate = new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider()).getCertificate(certificateHolder);
        } catch (CertificateException | OperatorCreationException e) {
            e.printStackTrace();
        }
        return certificate;

    }

    private static void store(
            String filename, char[] password,
            KeyPair generatedKeyPair) throws KeyStoreException, IOException,
            NoSuchAlgorithmException, CertificateException {

        X509Certificate rootCertificate = generateSelfSignedCertificate(generatedKeyPair);

        KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");
        pkcs12KeyStore.load(null, null);

        KeyStore.Entry entry = new KeyStore.PrivateKeyEntry(generatedKeyPair.getPrivate(),
                new X509Certificate[]{rootCertificate});
        KeyStore.ProtectionParameter param = new KeyStore.PasswordProtection(password);

        pkcs12KeyStore.setEntry("CA", entry, param);

        try (FileOutputStream fileOutputStream = new FileOutputStream(filename)) {
            pkcs12KeyStore.store(fileOutputStream, password);
        }
    }

}
