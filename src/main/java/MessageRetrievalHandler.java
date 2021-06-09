import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageRetrievalHandler extends Thread {

    private static final Logger LOGGER = Logger.getLogger(MessageRetrievalHandler.class.getName());
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
        //TODO: X.509 Certificate Decryption
        try {
            Object certificate = inputStream.readObject().toString();
            client.setOtherCertificate(certificate);
            System.out.println(certificate);
        } catch (IOException | ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
        }
        while (true) {
            try {
                //TODO: PGP Decryption
                Object message = inputStream.readObject();

                if (message instanceof Message) {
                    Message m = (Message) message;
                    byte[] data = Base64.getDecoder().decode(m.getBase64Image());

                    StringBuilder stringBuilder = new StringBuilder(client.getPath());
                    try (OutputStream stream = new FileOutputStream(stringBuilder.
                            append(ThreadLocalRandom.current().nextInt()).
                            append(".png").toString())) {
                        stream.write(data);
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, ex.getMessage());
                    }
                    System.out.println(m.getCaption());
                }

                if (message instanceof QuitMessage) {
                    QuitMessage m = (QuitMessage)message;
                    System.out.println(m);
                }

            } catch (IOException | ClassNotFoundException ex) {
                client.kill();
            }
        }
    }
}
