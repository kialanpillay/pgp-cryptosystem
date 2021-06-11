import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * <code>Prettier</code> is a concrete class that generates formatted output for clients.
 *
 * @author Kialan Pillay
 * @author Aidan Bailey
 * @author Insaaf Dhansay
 * @author Emily Morris
 * @version %I%, %G%
 */
public class Prettier {

    private final SimpleDateFormat sdf;

    /**
     * Class constructor.
     */
    public Prettier() {
        this.sdf = new SimpleDateFormat("HH:mm:ss");
    }

    /**
     * Class constructor specifying a {@link SimpleDateFormat} pattern.
     *
     * @param pattern pattern for {@link SimpleDateFormat} formatter
     */
    public Prettier(String pattern) {
        this.sdf = new SimpleDateFormat(pattern);
    }

    /**
     * Prints formatted output to the console.
     *
     * @param identity identity of the message source
     * @param message  message to print to console
     */
    public void print(String identity, String message) {
        StringBuilder stringBuilder = new StringBuilder();
        System.out.println(stringBuilder
                .append(sdf.format(new Date()))
                .append(" ")
                .append("[")
                .append(identity.toUpperCase())
                .append("]")
                .append(" - ")
                .append(message.toLowerCase()));
    }

    /**
     * Returns formatted output to a caller.
     *
     * @param identity identity of the message source
     * @param message  message to print to console
     * @return <code>String</code>
     */
    public String toString(String identity, String message) {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder
                .append(sdf.format(new Date()))
                .append(" ")
                .append("[")
                .append(identity.toUpperCase())
                .append("]")
                .append(" - ")
                .append(message.toLowerCase()).toString();
    }
}
