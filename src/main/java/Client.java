import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private static final Prettier prettier = new Prettier();
    private final String hostname;
    private final int port;
    private final Object certificate;
    private KeyGenerator keyGenerator;
    private KeyPair keyPair;
    private String username;
    private String path;
    private Object otherCertificate;
    private boolean certificateExchanged;
    private final boolean otherKeyAuthenticated;

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        try {
            this.keyGenerator = new KeyGenerator();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        try {
            this.keyPair = keyGenerator.generate(128);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //TODO
        this.certificate = null;
        this.otherCertificate = null;
        this.certificateExchanged = false;
        this.otherKeyAuthenticated = false;
    }

    public static void main(String[] args) {
        if (args.length < 2) return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        Client client = new Client(hostname, port);

        String username = "";
        String path = "";
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        prettier.print("System", "Welcome to CryptoSystem. I transfer your images more securely than FaceBook.");
        prettier.print("System", "Who are you?");
        try {
            username = stdin.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        prettier.print("System", "Where shall I store your images?");
        try {
            path = stdin.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        client.setUsername(username);
        client.setPath(path);
        client.connect();
    }

    public void connect() {
        try {
            Socket socket = new Socket(hostname, port);
            new MessageDispatchHandler(socket, this).start();
            new MessageRetrievalHandler(socket, this).start();

        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
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
        this.certificateExchanged = true;
    }

    public boolean isOtherKeyAuthenticated() {
        return otherKeyAuthenticated;
    }

    public boolean isCertificateExchanged() {
        return certificateExchanged;
    }

    public void debug(String message) {
        logger.info(message);
    }
}
