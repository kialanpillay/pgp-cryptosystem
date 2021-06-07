import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private final int port;
    private final Set<ClientHandler> handlers = new HashSet<>();

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
                handler.sendMessage(message);
            }
        }
    }

    public void terminateHandlers() {
        logger.info("Communication session terminated");
        handlers.clear();
    }
}
