/**
 * <code>AuthenticateMessage</code> is a concrete class that implements {@link CommandMessage}
 * which allow a client to notify the server that they have authenticated
 * the other party through certificate verification.
 *
 * @author Kialan Pillay
 * @version %I%, %G%
 */
public class AuthenticateMessage implements CommandMessage {

    private final String message;

    /**
     * Class constructor specifying message to transmit
     */
    public AuthenticateMessage(String message) {
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
