import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <code>Session</code> is a concrete class that stores state about
 * a communication session between clients. A <code>Session</code> can be in 1
 * of 2 states after it has been created (initiated by the server).
 * <ol>
 *     <li>
 *         Alive
 *     </li>
 *     <li>
 *         Active
 *     </li>
 * </ol>
 * A <code>Session</code> that is alive has two or more connected clients and
 * has been initiated by the {@link Server}.
 * For the purposes of this implementation, the upper bound on clients will be two.
 * A <code>Session</code> is active once certificates have been exchanged and
 * verified and each client has authenticated the other party by dispatching
 * an {@link AuthenticateMessage}.
 * A <code>Session</code> also stores a copy of each certificate and a record
 * of delivery to the other party. This is to prevent duplicate certificates being
 * delivered to a client.
 *
 * @author Kialan Pillay
 * @author Aidan Bailey
 * @author Insaaf Dhansay
 * @author Emily Morris
 * @version %I%, %G%
 */

public class Session {

    private final AtomicInteger authenticatedClients;
    private final Set<String> aliases = new HashSet<>();
    private final Map<String, X509Certificate> certificates = new HashMap<>();
    private final Map<String, Boolean> log = new HashMap<>();
    private volatile boolean alive;
    private volatile boolean active;

    public Session() {
        this.alive = false;
        this.active = false;
        this.authenticatedClients = new AtomicInteger(0);
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    /**
     * Returns the count of authenticated clients in a session.
     * A client is authenticated if it's certificate has been verified
     * by the other party.
     *
     * @return <code>AtomicInteger</code>
     */
    public AtomicInteger getAuthenticatedClients() {
        return authenticatedClients;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Increments the count of authenticated clients in a session.
     * Called by {@link ClientHandler} once it receives an {@link AuthenticateMessage}
     * message from a client.
     *
     * @return <code>AtomicInteger</code>
     */
    public void authenticate() {
        this.authenticatedClients.getAndIncrement();
    }

    public Map<String, X509Certificate> getCertificates() {
        return certificates;
    }

    //TODO

    /**
     * Returns the stored certificate for a specified client
     *
     * @return <code>Object</code>
     */
    public Object getCertificate(String alias) {
        return certificates.get(alias);
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public void storeAlias(String alias) {
        aliases.add(alias);
    }

    /**
     * Stores a client certificate in internal state
     * and adds a log entry to track certificate delivery
     *
     * @param certificate signed certificate containing client public key
     * @param alias    client alias
     */
    public void storeCertificate(X509Certificate certificate, String alias) {
        certificates.put(alias, certificate);
        log.put(alias, false);
    }

    public Map<String, Boolean> getLog() {
        return log;
    }

    /**
     * Updates the session log to record successful delivery
     * of a certificate
     *
     * @param alias alias of client attached to the certificate
     */
    public void log(String alias) {
        log.replace(alias, true);
    }

    /**
     * Returns the certificate delivery record for a specified client
     *
     * @param alias alias of client to poll
     * @return <code>boolean</code> returns <code>True</code> if certificate is delivered
     * <code>False</code> otherwise
     */
    public boolean isLogged(String alias) {
        return log.get(alias);
    }

    public void resetLog() {
        for (String k : log.keySet()) {
            log.replace(k, false);
        }
    }

    /**
     * Removes a client and associated from internal state
     *
     * @param alias alias of client to disconnect
     * @return <code>boolean</code> returns <code>True</code> if client is successfully disconnected
     * <code>False</code> otherwise
     */
    public boolean disconnectClient(String alias) {
        boolean disconnect = aliases.remove(alias);
        if (disconnect) {
            certificates.remove(alias);
            log.remove(alias);
            resetLog();
        }
        return disconnect;
    }
}
