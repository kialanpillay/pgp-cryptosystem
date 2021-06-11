import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>MessageRetrievalHandler</code> is a concrete class that extends {@link Thread}.
 * A dedicated handler thread is spawned by a <code>Client</code> after initiating a connection request.
 * A <code>MessageRetrievalHandler</code> is responsible
 * for retrieving encrypted messages from the <code>Server</code> after a communication session has been
 * activated, decrypting the messages using {@link PGPUtils} and delivering
 * it to a <code>Client</code>
 *
 * @author Kialan Pillay
 * @author Aidan Bailey
 * @author Insaaf Dhansay
 * @author Emily Morris
 * @version %I%, %G%
 * @see PGPUtils
 */
public class MessageRetrievalHandler extends Thread {

    private static final Logger LOGGER = Logger.getLogger(MessageRetrievalHandler.class.getName());
    private static final Prettier PRETTIER = new Prettier();
    private final Socket socket;
    private final Client client;
    private final ObjectInputStream inputStream;

    public MessageRetrievalHandler(Socket socket, ObjectInputStream inputStream, Client client) {
        this.socket = socket;
        this.client = client;
        this.inputStream = inputStream;
    }

    /**
     * Continuously reads in objects from an input stream after the thread is unblocked.
     * For each message retrieved, decrypts the message and writes the decoded image to disk.
     * Outputs the decrypted caption to console.
     *
     * @see PGPUtils
     */
    public void run() {
        while (!client.isOtherKeyAuthenticated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage());
            }
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
                    PRETTIER.print("Client", m.getCaption());
                    PRETTIER.print("System", "Enter the absolute path of an image to send.");
                }

                if (message instanceof QuitMessage) {
                    QuitMessage m = (QuitMessage) message;
                    PRETTIER.print("System", m.getMessage());
                }

            } catch (IOException | ClassNotFoundException ex) {
                client.kill();
            }
        }
    }
}
