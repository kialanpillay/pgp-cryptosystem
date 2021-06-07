import java.io.*;
import java.net.Socket;

public class Send extends Thread {

    private PrintWriter writer;
    private final Socket socket;
    private final Client client;

    public Send(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;
        try {
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        String message = "";

        do {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            try {
                message = client.getUsername() + " : " + input.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer.println(message);

        } while (!message.equals("quit"));

        try {
            socket.close();
        } catch (IOException ex) {
            System.out.println("Error writing to server: " + ex.getMessage());
        }
    }
}
