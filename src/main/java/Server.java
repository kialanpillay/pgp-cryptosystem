import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * <code>Server</code> is a concrete class that controls and manages communication
 * between two clients in a communication {@link Session}. A server instance is started
 * on a specified port and continuously listen for incoming client connections.
 * A dedicated <code>ClientHandler</code> is spawned to manage communication with each
 * client in parallel.
 *
 * @author Kialan Pillay
 * @author Aidan Bailey
 * @author Insaaf Dhansay
 * @author Emily Morris
 * @version %I%, %G%
 */
public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private final CommandMessageFactory COMMAND_MESSAGE_FACTORY = new CommandMessageFactory();
    private final int port;
    private final Set<ClientHandler> handlers = new HashSet<>();
    private Session session;

    /**
     * Class constructor.
     */
    public Server() {
        this.port = 4444;
    }

    /**
     * Class constructor specifying port
     */
    public Server(int port) {
        this.port = port;
    }

    /**
     * Starts an instance of the server. If no port is specified
     * as an argument, a server is created using the default port.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Server server = args.length > 1 ? new Server(Integer.parseInt(args[0])) : new Server();
        server.listen();
    }

    /**
     * Creates a {@link ServerSocket} on the specified port
     * to listen to incoming client connections.
     * Only terminates if the server is killed or an error is thrown
     * Once an incoming connection is accepted, a handler is spawned to
     * manage communication with the client
     */
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

        } catch (IOException | KeyStoreException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            kill();
        }
    }

    /**
     * Delivers an encrypted message to the other connected client
     * using the dedicated handler
     *
     * @param message message to deliver to client
     * @param source  handler that manages communication with the source client
     */
    public void deliver(Object message, ClientHandler source) throws IOException {
        for (ClientHandler handler : handlers) {
            if (handler != source) {
                handler.write(message);
                LOGGER.info("Delivered encrypted message to client " + getRecipientAlias(source.getAlias()));
            }
        }
    }

    /**
     * Delivers a {@link CommandMessage} to all connected clients.
     * Typically used to disconnect all clients.
     *
     * @param message command message to broadcast
     */
    public void broadcast(CommandMessage message) throws IOException {
        for (ClientHandler handler : handlers) {
            handler.write(message);
            LOGGER.info("Broadcast message to all connected clients");
        }
    }

    /**
     * Delivers a signed certificate to the connected client
     * using the dedicated handler
     *
     * @param source handler that manages communication with the source client
     */
    public void deliverCertificate(ClientHandler source) throws IOException, KeyStoreException {
        for (ClientHandler handler : handlers) {
            if (handler != source) {
                handler.write(session.getCertificate(source.getAlias()));
                LOGGER.info("Delivered X.509 certificate to client " + getRecipientAlias(source.getAlias()) + " from client " + source.getAlias());
                session.log(source.getAlias());
            }
        }
    }

    /**
     * Stores a client's alias in a {@link Session}
     *
     * @param alias client alias
     */
    public void storeAlias(String alias) {
        session.storeAlias(alias);
        LOGGER.info("Client " + alias + " has connected to the server");
        LOGGER.info("Connected clients " + session.getAliases());
    }

    /**
     * Retrieves alias of recipient client
     *
     * @param alias source client alias
     * @return <code>String</code>
     */
    public String getRecipientAlias(String alias) {
        return session.getAliases().stream().filter(a -> !a.equals(alias)).collect(Collectors.toList()).get(0);
    }

    /**
     * Stores a client certificate in a {@link Session}
     * If two clients have connected, a session is initiated
     *
     * @param certificate signed certificate containing client public key
     * @param alias       client alias
     */
    public void storeCertificate(X509Certificate certificate, String alias) {
        try {
            session.storeCertificate(certificate, alias);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
        }
        LOGGER.info("Cached X.509 certificate from client " + alias);
        if (session.getAliases().size() == 2) {
            initiateSession();
        }
    }

    /**
     * Disconnects a client if it belongs to a session.
     * Removes the associated handler and broadcasts a {@link QuitMessage}
     * to all client to force graceful disconnection
     *
     * @param alias   alias of client to disconnect
     * @param handler handler that manages communication with the specified client
     */
    public void disconnectClient(String alias, ClientHandler handler) {
        boolean disconnect = false;
        try {
            disconnect = session.disconnectClient(alias);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
        }
        if (disconnect) {
            handlers.remove(handler);
            LOGGER.info("Client " + alias + " has disconnected");
            try {
                String message = alias + " has left the matrix";
                CommandMessage quitMessage = COMMAND_MESSAGE_FACTORY.getCommandMessage("QUIT", message);
                broadcast(quitMessage);
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

    public boolean isSessionCertificateDelivered(String alias) {
        return session.isLogged(alias);
    }

    /**
     * Initiates a session after a second client
     * has connected to the server
     */
    public void initiateSession() {
        session.setAlive(true);
        LOGGER.info("Session initiated");
    }

    /**
     * Activates a session after each client has
     * authenticated the other post certificate exchange
     */
    public void activateSession() {
        session.setActive(true);
        LOGGER.info("Session activated");
    }

    /**
     * Terminates a {@link Session} and server instance
     * once a client has disconnected
     */
    public void terminateSession() {
        session.setAlive(false);
        session.setActive(false);
        LOGGER.info("Session terminated");
        kill();
    }

    /**
     * Terminates a running instance of the {@link Server}
     */
    public void kill() {
        LOGGER.info("Server shutting down");
        System.exit(0);
    }
}
