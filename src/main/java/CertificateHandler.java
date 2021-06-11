import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>CertificateHandler</code> is a concrete class that extends {@link Thread}.
 * A dedicated handler thread is spawned by a <code>Client</code> after accepting
 * an initiating a connection request. A <code>CertificateHandler</code> is responsible
 * for receiving a signed certificate from another client.
 *
 * @author Kialan Pillay
 * @author Aidan Bailey
 * @author Insaaf Dhansay
 * @author Emily Morris
 * @version %I%, %G%
 */
public class CertificateHandler extends Thread {

    private static final Logger LOGGER = Logger.getLogger(CertificateHandler.class.getName());
    private final Socket socket;
    private final Client client;
    private final ObjectInputStream inputStream;

    /**
     * Class constructor
     */
    public CertificateHandler(Socket socket, ObjectInputStream inputStream, Client client) {
        this.socket = socket;
        this.client = client;
        this.inputStream = inputStream;
    }

    /**
     * Reads an object from the input stream and casts it to a
     * <code>X509Certificate</code>.
     */
    public void run() {
        try {
            X509Certificate certificate = (X509Certificate) inputStream.readObject();
            client.storeOtherCertificate(certificate);
        } catch (IOException | ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
        }
    }
}
