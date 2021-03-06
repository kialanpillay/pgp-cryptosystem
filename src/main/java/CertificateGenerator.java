import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * <code>CertificateGenerator</code> is a concrete class that generates {@link X509Certificate}
 * certificates.
 *
 * @author Insaaf Dhansay
 * @author Kialan Pillay
 * @author Aidan Bailey
 * @author Emily Morris
 * @version %I%, %G%
 */
public class CertificateGenerator {

    /**
     * Sole class constructor
     */
    private CertificateGenerator() {

    }

    /**
     * Generates a signed public key certificate using a <code>SHA256WithRSAEncryption</code>
     * encryption algorithm.
     *
     * @param alias           alias of the certificate subject
     * @param clientPublicKey public key of a client
     * @param CAPrivateKey    private key of the Certificate Authority
     * @return <code>X509Certificate</code>
     */
    public static X509Certificate generate(String alias, PublicKey clientPublicKey, PrivateKey CAPrivateKey) {

        X500Name issuer = new X500Name("CN=CA");
        X500Name subject = new X500Name("CN=" + alias);
        Date before = new Date();
        Date after = new GregorianCalendar(2021, Calendar.DECEMBER, 31).getTime();
        BigInteger sn = new BigInteger(64, new SecureRandom());

        X509v3CertificateBuilder v3CertificateBuilder = new JcaX509v3CertificateBuilder(issuer, sn, before, after, subject, clientPublicKey);
        X509Certificate certificate = null;
        try {
            ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                    .setProvider(new BouncyCastleProvider())
                    .build(CAPrivateKey);
            X509CertificateHolder certificateHolder = v3CertificateBuilder.build(signer);
            certificate = new JcaX509CertificateConverter()
                    .setProvider(new BouncyCastleProvider())
                    .getCertificate(certificateHolder);
        } catch (CertificateException | OperatorCreationException e) {
            e.printStackTrace();
        }
        return certificate;

    }
}
