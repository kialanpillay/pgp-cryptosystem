import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {

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

            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client: " + socket);
                ClientHandler handler = new ClientHandler(socket, this);
                handlers.add(handler);
                handler.start();
            }

        } catch (IOException ex) {
            System.out.println("Error starting the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void deliver(String message, ClientHandler excludeUser) {
        for (ClientHandler handler : handlers) {
            if (handler != excludeUser) {
                handler.sendMessage(message);
            }
        }
    }

    public void terminateHandlers() {
        System.out.println("Session Terminated");
        handlers.clear();
    }
}
