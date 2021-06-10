/**
 * <code>CommandMessageFactory</code> is a concrete class that implements the factory pattern.
 * <code>CommandMessageFactory</code> abstracts the creation logic away from callers
 * and creates messages of type <code>CommandMessage</code>.
 *
 * @author Kialan Pillay
 * @author Aidan Bailey
 * @author Insaaf Dhansay
 * @author Emily Morris
 * @version %I%, %G%
 */
public class CommandMessageFactory {

    /**
     * Constructs a polymorphic <code>CommandMessage</code>
     *
     * @param type    type of message to construct
     * @param message message body
     * @return <code>CommandMessage</code>
     */
    public CommandMessage getCommandMessage(String type, String message) {
        if (type == null) {
            return null;
        }
        if (type.equalsIgnoreCase("QUIT")) {
            return new QuitMessage(message);
        } else if (type.equalsIgnoreCase("AUTH")) {
            return new AuthenticateMessage(message);
        }

        return null;
    }
}
