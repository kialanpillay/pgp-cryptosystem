import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageDispatchHandler extends Thread {

    private static final Logger logger = Logger.getLogger(MessageDispatchHandler.class.getName());
    private static final Prettier prettier = new Prettier();
    private final Socket socket;
    private final Client client;
    private final CommandMessageFactory commandMessageFactory = new CommandMessageFactory();
    private ObjectOutputStream outputStream;

    public MessageDispatchHandler(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            logger.log(Level.WARNING, ex.getMessage());
        }
    }

    public static String encodeImageToBase64(File file) {
        String base64Image = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStream.read(bytes);
            base64Image = Base64.getEncoder().encodeToString(bytes);
        } catch (IOException ex) {
            logger.log(Level.WARNING, ex.getMessage());
        }

        return base64Image;
    }

    public void run() {

        try {
            outputStream.writeObject(client.getUsername());
        } catch (IOException ex) {
            logger.log(Level.WARNING, ex.getMessage());
        }

        //TODO: client.getCertificate()
        String certificate = "cert" + client.getUsername();
        try {
            outputStream.writeObject(certificate);
        } catch (IOException ex) {
            logger.log(Level.WARNING, ex.getMessage());
        }

        //TODO: Certificate Verification

        while(!client.isRecipientKeyAuthenticated()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                logger.log(Level.WARNING, ex.getMessage());
            }
        }

        //Key Validated

        CommandMessage commandMessage = commandMessageFactory.getCommandMessage("AUTH", client.getUsername());
        try {
            outputStream.writeObject(commandMessage);
        } catch (IOException ex) {
            logger.log(Level.WARNING, ex.getMessage());
        }

        Object message = null;
        String input = "";
        do {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            try {
                input = stdin.readLine();

                if (input.equals("quit")) {
                    message = commandMessageFactory.getCommandMessage("QUIT", client.getUsername());
                } else {
                    Path path = Paths.get(input);

                    while (Files.notExists(path)) {
                        prettier.print("System", "Invalid!");
                        path = Paths.get(stdin.readLine());
                    }
                    String caption = stdin.readLine();
                    File file = path.toFile();

                    //TODO: PGP Encryption
                    message = new Message(encodeImageToBase64(file), caption);
                }

            } catch (IOException ex) {
                logger.log(Level.WARNING, ex.getMessage());
            }

            try {
                outputStream.writeObject(message);
            } catch (IOException ex) {
                logger.log(Level.WARNING, ex.getMessage());
            }

        } while (!input.equals("quit"));

        try {
            socket.close();
        } catch (IOException ex) {
            logger.log(Level.WARNING, ex.getMessage());
        }
    }
}
