import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static final Prettier prettier = new Prettier();
    private final int port;
    private final Set<ClientHandler> handlers = new HashSet<>();
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

            logger.info("Server listening on port " + port);
            session = new Session();
            logger.info("Session created");

            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("Created socket at port " + socket.getPort());
                ClientHandler handler = new ClientHandler(socket, this);
                handlers.add(handler);
                handler.start();
            }

        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
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

    public void broadcast(String message) throws IOException {
        for (ClientHandler handler : handlers) {
            handler.write(message);
        }
    }

    public void deliverCertificate(String username, ClientHandler source) throws IOException {
        for (ClientHandler handler : handlers) {
            if (handler != source) {
                handler.write(session.getCertificate(username));
                logger.info("Delivered X.509 certificate from " + username);
                session.dispatchCertificate(username);
                logger.info("Dispatched certificates " + session.getDispatchedCertificates());
            }
        }
    }

    public void storeUsername(String username) {
        session.storeUsername(username);
        logger.info("Connected clients " + session.getUsernames());
    }

    public void storeCertificate(Object certificate, String username) {
        session.storeCertificate(certificate, username);
        logger.info("Received X.509 certificate from " + username);
        logger.info("Session certificates " + session.getCertificates());
        if (session.getUsernames().size() == 2) {
            initiateSession();
        }
    }

    public void disconnectClient(String username, ClientHandler handler) {
        boolean disconnect = session.disconnectClient(username);
        if (disconnect) {
            handlers.remove(handler);
            logger.info("Client " + username + " has disconnected");
            try {
                broadcast(prettier.toString("Server", username + " has left the matrix."));
            } catch (IOException ex) {
                logger.log(Level.WARNING, ex.getMessage());
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
        return session.isDispatched(username);
    }

    public void initiateSession() {
        session.setAlive(true);
        logger.info("Session initiated");
    }

    public void activateSession() {
        session.setActive(true);
        logger.info("Session activated");
    }

    public void terminateSession() {
        session.setAlive(false);
        session.setActive(false);
        logger.info("Session terminated");
    }

    public void kill() {
        System.exit(0);
    }
}
