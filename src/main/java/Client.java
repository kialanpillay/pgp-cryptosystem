import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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
    private static final SecretsManager SECRETS_MANAGER = new SecretsManager();
    private final String hostname;
    private final int port;
    private final PublicKey CAPublicKey;
    private final KeyPair keyPair;
    private X509Certificate certificate;
    private X509Certificate otherCertificate;
    private boolean otherKeyAuthenticated;
    private String username;
    private String path;

    /**
     * Class constructor specifying server hostname and port
     */
    public Client(String hostname, int port) throws NoSuchAlgorithmException {
        this.hostname = hostname;
        this.port = port;
        this.CAPublicKey = SECRETS_MANAGER.getPublicKey();
        this.keyPair = KeyUtils.generate("RSA", 1024);
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
    public static void main(String[] args) throws NoSuchAlgorithmException {
        String hostname = "localhost";
        int port = 4444;

        if (args.length != 0 && args.length != 2) {
            return;
        } else if (args.length == 2) {
            hostname = args[0];
            port = Integer.parseInt(args[1]);
        }

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
        client.getCASignedCertificate();
        client.connect();
    }

    /**
     * Generates a certificate containing the public key of the client
     * signed using the private key of the Certificate Authority
     */
    private void getCASignedCertificate() {
        this.certificate = SECRETS_MANAGER.generateCertificate(this.username, this.keyPair.getPublic());
    }

    /**
     * Creates a socket using the specified hostname and port
     * of the server. Spawns a {@link MessageDispatchHandler}
     * and {@link MessageRetrievalHandler} thread to handle inbound
     * and outbound communications
     */
    private void connect() {
        try {
            Socket socket = new Socket(hostname, port);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            new AuthenticationHandler(socket, outputStream, this).start();
            new CertificateHandler(socket, inputStream, this).start();
            new MessageDispatchHandler(socket, outputStream, this).start();
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

    public X509Certificate getCertificate() {
        return certificate;
    }

    public X509Certificate getOtherCertificate() {
        return otherCertificate;
    }

    public void setOtherCertificate(X509Certificate otherCertificate) {
        this.otherCertificate = otherCertificate;
    }

    public void verifyOtherCertificate() {
        try {
            this.otherCertificate.verify(CAPublicKey);
            System.out.println(this.otherCertificate.getPublicKey());
            this.otherKeyAuthenticated = true;

        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
            e.printStackTrace();
            this.otherKeyAuthenticated = false;
        }
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
