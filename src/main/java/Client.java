import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
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
 * @author Aidan Bailey
 * @author Insaaf Dhansay
 * @author Emily Morris
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
    private KeyStore keyStore;
    private boolean otherKeyAuthenticated;
    private String alias;
    private String path;

    /**
     * Class constructor specifying server hostname and port
     */
    public Client(String hostname, int port) throws NoSuchAlgorithmException {
        this.hostname = hostname;
        this.port = port;
        this.CAPublicKey = SECRETS_MANAGER.getPublicKey();
        this.keyPair = KeyUtils.generate("RSA", 1024);
        this.otherKeyAuthenticated = false;
        loadKeyStore();
    }

    /**
     * Initialises a PKCS12 keystore for in-memory storage
     * of secrets
     */
    private void loadKeyStore() {
        try {
            this.keyStore = KeyStore.getInstance("PKCS12");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        try {
            this.keyStore.load(null, null);
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts a client instance using the specified hostname and port.
     * If no hostname or port is specified the operation is aborted.
     * Retrieves the client's alias and output directory from the console
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

        String alias = "";
        String path = "";
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        PRETTIER.print("System", "Welcome to CryptoSystem. Safe. Secure. Communication.");
        PRETTIER.print("System", "Alias?");
        try {
            alias = stdin.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        PRETTIER.print("System", "Directory?");
        try {
            path = stdin.readLine();
            while (!Files.isDirectory(Paths.get(path))) {
                PRETTIER.print("System", "This is not a valid directory. Try again.");
                path = stdin.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        PRETTIER.print("System", "Please wait until a secure session is established.");

        client.setAlias(alias);
        client.setPath(path);
        client.getCASignedCertificate();
        client.connect();
    }

    /**
     * Generates a certificate containing the public key of the client
     * signed using the private key of the Certificate Authority.
     * Stores the certificate in an in-memory key store.
     */
    private void getCASignedCertificate() {
        X509Certificate certificate = SECRETS_MANAGER.generateCertificate(this.alias, this.keyPair.getPublic());
        try {
            keyStore.setCertificateEntry(alias, certificate);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
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

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public X509Certificate getCertificate() throws KeyStoreException {
        return (X509Certificate) keyStore.getCertificate(alias);
    }

    /**
     * Stores a certificate from a connected client
     * in the in-memory key store and initiates verification.
     *
     * @param otherCertificate signed certificate of other client
     */
    public void storeOtherCertificate(X509Certificate otherCertificate) {
        try {
            keyStore.setCertificateEntry("other", otherCertificate);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        verifyOtherCertificate();
    }

    /**
     * Verifies the authenticity of a client using it's certificate.
     * The certificate is verified using the public key
     * of the trusted Certificate Authority.
     * If the certificate is unverified a <code>InvalidKeyException</code>
     * is thrown.
     */
    public void verifyOtherCertificate() {
        try {
            this.keyStore.getCertificate("other").verify(CAPublicKey);
            this.otherKeyAuthenticated = true;

        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException | KeyStoreException e) {
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
