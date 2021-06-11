import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>MessageDispatchHandler</code> is a concrete class that extends {@link Thread}.
 * A dedicated handler thread is spawned by a <code>Client</code> after initiating a connection request.
 * A <code>MessageDispatchHandler</code> is responsible
 * for dispatching encrypted messages to the <code>Server</code> after a communication session has been
 * activated.
 *
 * @author Kialan Pillay
 * @author Aidan Bailey
 * @author Insaaf Dhansay
 * @author Emily Morris
 * @version %I%, %G%
 */
public class MessageDispatchHandler extends Thread {

    private static final Logger LOGGER = Logger.getLogger(MessageDispatchHandler.class.getName());
    private static final Prettier PRETTIER = new Prettier();
    private final Socket socket;
    private final Client client;
    private final CommandMessageFactory commandMessageFactory = new CommandMessageFactory();
    private final ObjectOutputStream outputStream;

    /**
     * Sole class constructor
     */
    public MessageDispatchHandler(Socket socket, ObjectOutputStream outputStream, Client client) {
        this.socket = socket;
        this.client = client;
        this.outputStream = outputStream;
    }

    /**
     * Converts an image file to <code>Base64</code> encoded string
     *
     * @param file image to encode
     * @returns <code>String</code>
     */
    public static String encodeImageToBase64(File file) {
        String base64Image = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStream.read(bytes);
            base64Image = Base64.getEncoder().encodeToString(bytes);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
        }

        return base64Image;
    }

    /**
     * Reads in input from the console after the thread is unblocked
     * and continuously writes encrypted messages to the socket output stream.
     * Writes a {@link QuitMessage} to the output stream if the client requests to disconnect
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
        try {
            PRETTIER.print("System", "The identity of " + client.getOtherAlias() + " has been authenticated.");
        } catch (KeyStoreException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
        }
        PRETTIER.print("System", "The secure session will be activated now.");

        Object message = null;
        String input = "";
        do {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            try {
                PRETTIER.print("System", "Enter the absolute path of an image to transfer.");
                input = stdin.readLine();

                if (input.equals("quit")) {
                    message = commandMessageFactory.getCommandMessage("QUIT", client.getAlias());
                } else {
                    Path path = Paths.get(input);

                    while (Files.notExists(path)) {
                        PRETTIER.print("System", "The image cannot be located. Try again.");
                        path = Paths.get(stdin.readLine());
                    }
                    PRETTIER.print("System", "Enter a caption for the image.");
                    String caption = stdin.readLine();
                    File file = path.toFile();

                    Message m = new Message(encodeImageToBase64(file), caption);
                    message = encode(m);
                }

            } catch (IOException | KeyStoreException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage());
            }

            try {
                outputStream.writeObject(message);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage());
            }

        } while (!input.equals("quit"));
    }

    private byte[] encode(Message message) throws KeyStoreException, InvalidAlgorithmParameterException,
            NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            BadPaddingException, InvalidKeyException {
        return PGPUtils.PGPEncode(message, client.getPrivateKey(), client.getOtherPublicKey(), Client.LOGGER);
    }
}
