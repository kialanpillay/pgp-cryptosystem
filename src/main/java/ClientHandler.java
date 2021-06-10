import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>ClientHandler</code> is a concrete class that extends {@link Thread}.
 * A dedicated handler thread is spawned by the <code>Server</code> after accepting
 * an incoming connection request. A <code>ClientHandler</code> is responsible
 * for retrieving dispatched messages from a client and delivering
 * messages to a client on behalf of the server. <code>ClientHandler</code> reads from an
 * {@link ObjectInputStream} and writes to an {@link ObjectOutputStream}.
 *
 * @author Kialan Pillay
 * @author Aidan Bailey
 * @author Insaaf Dhansay
 * @author Emily Morris
 * @version %I%, %G%
 */
public class ClientHandler extends Thread {

    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    private final Socket socket;
    private final Server server;
    private ObjectOutputStream outputStream;

    /**
     * Class constructor specifying client socket and server instance
     */
    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    /**
     * Retrieves the client's alias and certificate from
     * the <code>ObjectInputStream</code> and stores the certificate in
     * the {@link Session}. The thread is paused until another client connects
     * and the session is initiated. Once a session is alive, if the certificate is
     * yet to be delivered, the handler sends a request to the server.
     * Once an {@link AuthenticateMessage} is received from the client, it
     * authenticates the client in the session.
     * Once a session is activated, the handler continuously retrieves
     * messages from the client and passes them to the server for delivery
     * to the destination. If a {@link QuitMessage} is received, the client is
     * disconnected and the socket is closed.
     */
    public void run() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());

            String alias = inputStream.readObject().toString();
            server.storeAlias(alias);

            X509Certificate certificate;
            certificate = (X509Certificate)inputStream.readObject();
            server.storeCertificate(certificate, alias);

            while (!server.isSessionAlive()) {
                Thread.sleep(100);
            }

            if (server.isSessionAlive()) {
                if (!server.isSessionCertificateDelivered(alias)) {
                    server.deliverCertificate(alias, this);
                }
                try {
                    Object message = inputStream.readObject();

                    if (message instanceof AuthenticateMessage) {
                        server.authenticateClient();
                    }

                    if (server.getSessionAuthenticatedClients().get() == 2) {
                        server.activateSession();
                    }

                } catch (IOException | ClassNotFoundException ex) {
                    server.kill();
                }
            }


            while (true) {
                if (server.isSessionActive()) {
                    try {

                        Object message = null;
                        do {
                            try {
                                message = inputStream.readObject();
                            } catch (IOException | ClassNotFoundException ex) {
                                LOGGER.log(Level.WARNING, ex.getMessage());
                            }
                            if (message instanceof Message) {
                                Message m = (Message) message;
                                server.deliver(m, this);
                            }

                        } while (message instanceof Message);


                        server.disconnectClient(alias, this);
                        socket.close();

                    } catch (IOException ex) {
                        server.kill();
                    }
                } else {
                    Thread.sleep(100);
                }
            }
        } catch (IOException | ClassNotFoundException | InterruptedException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage());
            server.kill();
        }


    }

    /**
     * Delivers a {@link Message} to a client by writing
     * to the <code>OutputStream</code> attached to it's socket
     *
     * @param message message to deliver to recipient
     */
    public void write(Message message) throws IOException {
        outputStream.writeObject(message);
    }

    //TODO

    /**
     * Delivers a {@link Message} to a client by writing
     * to the <code>OutputStream</code> attached to it's socket
     *
     * @param certificate client certificate to deliver to recipient
     */
    public void write(Object certificate) throws IOException {
        outputStream.writeObject(certificate);
    }
}
