import java.io.Serializable;

/**
 * <code>CommandMessage</code> is an interface that implements {@link CommandMessage}
 * which allow a client to notify the server that they have authenticated
 * the recipient through certificate verification
 *
 * @author Kialan Pillay
 * @author Aidan Bailey
 * @author Insaaf Dhansay
 * @author Emily Morris
 * @version %I%, %G%
 */
public interface CommandMessage extends Serializable {

    String message = "";

    /**
     * Gets the message for this {@link CommandMessage}
     *
     * @return <code>String</code>>
     */
    String getMessage();
}
