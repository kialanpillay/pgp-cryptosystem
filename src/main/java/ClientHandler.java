import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler extends Thread {

    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
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

            String username = inputStream.readObject().toString();
            server.storeUsername(username);

            //TODO
            Object certificate;
            certificate = inputStream.readObject().toString();
            server.storeCertificate(certificate, username);

            while (true) {

                if (server.isSessionActive()) {
                    try {

                        Object message = null;
                        do {
                            try {
                                message = inputStream.readObject();
                            } catch (IOException | ClassNotFoundException ex) {
                                logger.log(Level.WARNING, ex.getMessage());
                            }

                            if (message instanceof Message) {
                                Message m = (Message) message;
                                server.deliver(m, this);
                            }

                        } while (message instanceof Message);

                        server.terminateSession();
                        server.disconnectClient(username, this);
                        socket.close();

                    } catch (IOException ex) {
                        logger.log(Level.WARNING, ex.getMessage());
                    }
                } else if (server.isSessionAlive()) {
                    if(!server.isSessionCertificateDelivered(username)){
                        server.deliverCertificate(username,this);
                    }

                    try {
                        Object message = inputStream.readObject();

                        if (message instanceof AuthenticatedMessage) {
                            server.authenticateClient();
                        }

                        server.debug("Authenticated client " + server.getSessionAuthenticatedClients().get());

                        if (server.getSessionAuthenticatedClients().get() == 2) {
                            server.activateSession();
                        }

                    } catch (IOException | ClassNotFoundException ex) {
                        logger.log(Level.WARNING, ex.getMessage());
                    }
                }
                else{
                    Thread.sleep(100);
                }
            }
        } catch (IOException | ClassNotFoundException | InterruptedException ex) {
            logger.log(Level.WARNING, ex.getMessage());
        }
    }

    public void write(Message message) throws IOException {
        outputStream.writeObject(message);
    }

    //TODO
    public void write(Object certificate) throws IOException {
        outputStream.writeObject(certificate);
    }
}
