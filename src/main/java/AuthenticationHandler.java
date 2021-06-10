import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>AuthenticationHandler</code> is a concrete class that extends {@link Thread}.
 * A dedicated handler thread is spawned by the <code>Client</code> after accepting
 * an initiating a connection request. A <code>AuthenticationHandler</code> is responsible
 * for sending the username and certificate of the client to the server. Once the certificates
 * have been exchanged and verified, the handler dispatches a {@link AuthenticateMessage}
 * to the <code>Server</code>. Once both clients have authenticated, the session is activated.
 *
 * @see CertificateHandler
 * @author Kialan Pillay
 * @version %I%, %G%
 */
public class AuthenticationHandler extends Thread {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationHandler.class.getName());
    private final Socket socket;
    private final Client client;
    private final CommandMessageFactory commandMessageFactory = new CommandMessageFactory();
    private ObjectOutputStream outputStream;

    public AuthenticationHandler(Socket socket, ObjectOutputStream outputStream, Client client) {
        this.socket = socket;
        this.client = client;
        this.outputStream = outputStream;
    }

    public void run() {

        try {
            outputStream.writeObject(client.getUsername());
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
        }

        //TODO: client.getCertificate()
        String certificate = "X.509CERT - " + client.getUsername();
        try {
            outputStream.writeObject(certificate);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
        }

        //TODO: Certificate Verification

        while (!client.isOtherKeyAuthenticated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage());
            }
        }

        CommandMessage commandMessage = commandMessageFactory.getCommandMessage("AUTH", client.getUsername());
        try {
            outputStream.writeObject(commandMessage);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
        }
    }
}
