import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    private final String hostname;
    private final int port;
    private String username;

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public static void main(String[] args) {
        if (args.length < 2) return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        Client client = new Client(hostname, port);

        String username = "";
        System.out.println("Enter Username: ");
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        try {
            username = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.setUsername(username);

        client.connect();
    }

    public void connect() {
        try {
            Socket socket = new Socket(hostname, port);
            new Send(socket, this).start();
            new Receive(socket, this).start();

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }

    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
