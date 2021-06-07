import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageRetrievalHandler extends Thread {

    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private final Socket socket;
    private final Client client;
    private ObjectInputStream inputStream;

    public MessageRetrievalHandler(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;
        try {
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                Message message = (Message) inputStream.readObject();
                byte[] data = Base64.getDecoder().decode(message.getBase64Image());
                try (OutputStream stream = new FileOutputStream(client.getDirectory())) {
                    stream.write(data);
                } catch (IOException ex) {
                    logger.log(Level.WARNING, ex.getMessage());
                }
                System.out.println(message.getCaption());
            } catch (IOException | ClassNotFoundException ex) {
                logger.log(Level.WARNING, ex.getMessage());
                break;
            }
        }
    }
}
