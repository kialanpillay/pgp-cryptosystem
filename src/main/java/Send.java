import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Send extends Thread {

    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static final Prettier prettier = new Prettier();
    private final Socket socket;
    private final Client client;
    private ObjectOutputStream outputStream;

    public Send(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String encodeImageToBase64(File file) {
        String base64Image = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStream.read(bytes);
            base64Image = Base64.getEncoder().encodeToString(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return base64Image;
    }

    public void run() {

        Message message = null;
        String input = "";
        do {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            try {
                input = stdin.readLine();

                if (input.equals("quit")) {
                    message = new Message(null, "quit");
                } else {
                    Path path = Paths.get(input);

                    while (Files.notExists(path)) {
                        prettier.print("System", "Invalid! ");
                        path = Paths.get(stdin.readLine());
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    String caption = stringBuilder.append(stdin.readLine()).toString();
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
