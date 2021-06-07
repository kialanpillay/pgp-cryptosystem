import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler extends Thread {

    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private final Socket socket;
    private final Server server;
    private ObjectOutputStream outputStream;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());

            Message message;
            do {
                try {
                    message = (Message) inputStream.readObject();
                } catch (IOException | ClassNotFoundException ex) {
                    logger.log(Level.WARNING, ex.getMessage());
                    break;
                }

                if (!message.getCaption().equals("quit")) {
                    server.deliver(message, this);
                }

            } while (!message.getCaption().equals("quit"));

            server.terminateHandlers();
            socket.close();

        } catch (IOException ex) {
            logger.log(Level.WARNING, ex.getMessage());
        }
    }

    public void sendMessage(Message message) throws IOException {
        outputStream.writeObject(message);
    }
}
