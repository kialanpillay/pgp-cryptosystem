import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>Client</code> is a concrete class that represents a connected
 * client. A <code>Client</code> generates a public-private key pair and
 * a signed certificate with it's public key. The certificate is used to
 * authenticate the communication {@link Session}. A <code>Client</code>
 * spawns two handlers that control the dispatch and retrieval of messages
 * to and from the {@link Server} to facilitate the simultaneous exchange
 * of encrypted messages.
 *
 * @author Kialan Pillay
 * @version %I%, %G%
 */
public class Client {

    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private static final Prettier PRETTIER = new Prettier();
    private final String hostname;
    private final int port;
    private final Object certificate;
    private final boolean otherKeyAuthenticated;
    private KeyPair keyPair;
    private String username;
    private String path;
    private Object otherCertificate;

    /**
     * Class constructor specifying server hostname and port
     */
    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;

        try {
            this.keyPair = KeyUtils.generate("RSA", 1024);
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
        }
        //TODO
        this.certificate = null;
        this.otherCertificate = null;
        this.otherKeyAuthenticated = false;
    }

    /**
     * Starts a client instance using the specified hostname and port.
     * If no hostname or port is specified the operation is aborted.
     * Retrieves the client's username and output directory from the console
     * and initiates a connection request to the server.
     *
     * @param args command line parameters
     */
    public static void main(String[] args) {
        if (args.length < 2) return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        Client client = new Client(hostname, port);

        String username = "";
        String path = "";
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        PRETTIER.print("System", "Welcome to CryptoSystem. I transfer your images more securely than FaceBook.");
        PRETTIER.print("System", "Who are you?");
        try {
            username = stdin.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        PRETTIER.print("System", "Where shall I store your images?");
        try {
            path = stdin.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        client.setUsername(username);
        client.setPath(path);
        client.connect();
    }

    /**
     * Creates a socket using the specified hostname and port
     * of the server. Spawns a {@link MessageDispatchHandler}
     * and {@link MessageRetrievalHandler} thread to handle inbound
     * and outbound communications
     */
    public void connect() {
        try {
            Socket socket = new Socket(hostname, port);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            new AuthenticationHandler(socket, outputStream,this).start();
            new CertificateHandler(socket, inputStream, this).start();
            new MessageDispatchHandler(socket, outputStream,this).start();
            new MessageRetrievalHandler(socket, inputStream, this).start();

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
        }

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Object getCertificate() {
        return certificate;
    }

    public Object getOtherCertificate() {
        return otherCertificate;
    }

    public void setOtherCertificate(Object otherCertificate) {
        this.otherCertificate = otherCertificate;
    }

    public boolean isOtherKeyAuthenticated() {
        return otherKeyAuthenticated;
    }

    public void debug(String message) {
        LOGGER.info(message);
    }

    /**
     * Terminates a running instance of the {@link Client}
     */
    public void kill() {
        PRETTIER.print("System", "You are being disconnected from CryptoSystem.");
        System.exit(0);
    }
}
