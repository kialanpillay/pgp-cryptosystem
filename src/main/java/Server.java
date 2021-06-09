import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private static final Prettier PRETTIER = new Prettier();
    private int port;
    private Set<ClientHandler> handlers = new HashSet<>();
    private final CommandMessageFactory COMMAND_MESSAGE_FACTORY = new CommandMessageFactory();
    private Session session;

    public Server() {
        this.port = 4444;
    }

    public Server(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        Server server = args.length > 1 ? new Server(Integer.parseInt(args[0])) : new Server();
        server.listen();
    }

    public void listen() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            LOGGER.info("Server listening on port " + port);
            session = new Session();

            while (true) {
                Socket socket = serverSocket.accept();
                LOGGER.info("Created socket at port " + socket.getPort());
                ClientHandler handler = new ClientHandler(socket, this);
                handlers.add(handler);
                handler.start();
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void deliver(Message message, ClientHandler source) throws IOException {
        for (ClientHandler handler : handlers) {
            if (handler != source) {
                handler.write(message);
            }
        }
    }

    public void deliver(CommandMessage message) throws IOException {
        for (ClientHandler handler : handlers) {
            handler.write(message);
        }
    }

    public void deliverCertificate(String username, ClientHandler source) throws IOException {
        for (ClientHandler handler : handlers) {
            if (handler != source) {
                handler.write(session.getCertificate(username));
                LOGGER.info("Delivered X.509 certificate from " + username);
                session.log(username);
            }
        }
    }

    public void storeUsername(String username) {
        session.storeUsername(username);
        LOGGER.info("Connected clients " + session.getUsernames());
    }

    public void storeCertificate(Object certificate, String username) {
        session.storeCertificate(certificate, username);
        LOGGER.info("Received X.509 certificate from " + username);
        LOGGER.info("Session certificates " + session.getCertificates());
        if (session.getUsernames().size() == 2) {
            initiateSession();
        }
    }

    public void disconnectClient(String username, ClientHandler handler) {
        boolean disconnect = session.disconnectClient(username);
        if (disconnect) {
            handlers.remove(handler);
            LOGGER.info("Client " + username + " has disconnected");
            try {
                String message = PRETTIER.toString("System", username + " has left the matrix.");
                CommandMessage quitMessage = COMMAND_MESSAGE_FACTORY.getCommandMessage("QUIT", message);
                deliver(quitMessage);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage());
            }
            terminateSession();
        }

    }

    public void authenticateClient() {
        session.authenticate();
    }

    public AtomicInteger getSessionAuthenticatedClients() {
        return session.getAuthenticatedClients();
    }

    public boolean isSessionAlive() {
        return session.isAlive();
    }

    public boolean isSessionActive() {
        return session.isActive();
    }

    public boolean isSessionCertificateDelivered(String username) {
        return session.isLogged(username);
    }

    public void initiateSession() {
        session.setAlive(true);
        LOGGER.info("Session initiated");
    }

    public void activateSession() {
        session.setActive(true);
        LOGGER.info("Session activated");
    }

    public void terminateSession() {
        session.setAlive(false);
        session.setActive(false);
        LOGGER.info("Session terminated");
        kill();
    }

    public void kill() {
        LOGGER.info("Server shutting down");
        System.exit(0);
    }
}
