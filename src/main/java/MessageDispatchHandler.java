import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageDispatchHandler extends Thread {

    private static final Logger LOGGER = Logger.getLogger(MessageDispatchHandler.class.getName());
    private static final Prettier PRETTIER = new Prettier();
    private final Socket socket;
    private final Client client;
    private final CommandMessageFactory commandMessageFactory = new CommandMessageFactory();
    private final ObjectOutputStream outputStream;

    public MessageDispatchHandler(Socket socket, ObjectOutputStream outputStream, Client client) {
        this.socket = socket;
        this.client = client;
        this.outputStream = outputStream;
    }

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

    public void run() {
        while (!client.isOtherKeyAuthenticated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage());
            }
        }
        PRETTIER.print("System", "The identity of the other party has been authenticated.");
        PRETTIER.print("System", "The secure session will be activated shortly.");

        Object message = null;
        String input = "";
        do {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            try {
                PRETTIER.print("System", "Enter the absolute path of an image to send.");
                input = stdin.readLine();

                if (input.equals("quit")) {
                    message = commandMessageFactory.getCommandMessage("QUIT", client.getAlias());
                } else {
                    Path path = Paths.get(input);

                    while (Files.notExists(path)) {
                        PRETTIER.print("System", "The image cannot be located. Try again.");
                        path = Paths.get(stdin.readLine());
                    }
                    PRETTIER.print("System", "Enter a caption for the image. ");
                    String caption = stdin.readLine();
                    File file = path.toFile();

                    //TODO: PGP Encryption
                    message = new Message(encodeImageToBase64(file), caption);
                }

            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage());
            }

            try {
                outputStream.writeObject(message);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage());
            }

        } while (!input.equals("quit"));
    }
}
