/**
 * <code>QuitMessage</code> is a concrete class that implements {@link CommandMessage}
 * which allow a client to dispatch a disconnection request to the server.
 *
 * @author Kialan Pillay
 * @author Aidan Bailey
 * @author Insaaf Dhansay
 * @author Emily Morris
 * @version %I%, %G%
 */
public class QuitMessage implements CommandMessage {

    private final String message;

    /**
     * Class constructor specifying message to transmit
     */
    public QuitMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the message for this {@link CommandMessage}
     *
     * @return <code>String</code>>
     */
    @Override
    public String getMessage() {
        return message;
    }
}
