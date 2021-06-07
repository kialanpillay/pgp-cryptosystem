import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static final Prettier prettier = new Prettier();
    private final String hostname;
    private final int port;
    private String username;
    private String directory;

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
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

        } catch (UnknownHostException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
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
}
