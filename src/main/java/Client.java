import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private static final Prettier prettier = new Prettier();
    private final String hostname;
    private final int port;
    private String username;
    private String directory;
    private Object certificate;
    private Object recipientCertificate;
    private boolean certificateExchanged;
    private boolean recipientKeyAuthenticated;

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        //TODO
        this.certificate = null;
        this.recipientCertificate = null;
        this.certificateExchanged = false;
        this.recipientKeyAuthenticated = false;
    }

    public static void main(String[] args) {
        if (args.length < 2) return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        Client client = new Client(hostname, port);

        String username = "";
        String dir = "";
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        prettier.print("System", "Username? ");
        try {
            username = stdin.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        prettier.print("System", "Directory? ");
        try {
            dir = stdin.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        client.setUsername(username);
        client.setDirectory(dir);
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

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String path) {
        this.directory = path;
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

    public Object getRecipientCertificate() {
        return recipientCertificate;
    }

    public void setRecipientCertificate(Object recipientCertificate) {
        this.recipientCertificate = recipientCertificate;
        this.certificateExchanged = true;
    }

    public boolean isRecipientKeyAuthenticated() {
        return recipientKeyAuthenticated;
    }

    public void setRecipientKeyAuthenticated(boolean recipientKeyAuthenticated) {
        this.recipientKeyAuthenticated = recipientKeyAuthenticated;
    }

    public boolean isCertificateExchanged() {
        return certificateExchanged;
    }

    public void debug(String message){
        logger.info(message);
    }
}
