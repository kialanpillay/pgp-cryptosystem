import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Session {

    private final AtomicInteger authenticatedClients;
    private boolean alive;
    private boolean active;
    private boolean authenticated;
    private final Set<String> usernames = new HashSet<>();
    private final Map<String, Object> certificates = new HashMap<>();
    private final Map<String, Boolean> dispatchedCertificates = new HashMap<>();

    public Session() {
        this.alive = false;
        this.active = false;
        this.authenticated = false;
        this.authenticatedClients = new AtomicInteger(0);
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public AtomicInteger getAuthenticatedClients() {
        return authenticatedClients;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public void authenticate() {
        this.authenticatedClients.getAndIncrement();
    }

    public Map<String, Object> getCertificates() {
        return certificates;
    }

    //TODO
    public Object getCertificate(String username) {
        return certificates.get(username);
    }

    public Set<String> getUsernames() {
        return usernames;
    }

    public void storeUsername(String username) {
        usernames.add(username);
    }

    public void storeCertificate(Object certificate, String username) {
        certificates.put(username, certificate);
        dispatchedCertificates.put(username, false);
    }

    public Map<String, Boolean> getDispatchedCertificates() {
        return dispatchedCertificates;
    }

    public void dispatchCertificate(String username) {
        dispatchedCertificates.replace(username, true);
    }

    public boolean isDispatched(String username) {
        return dispatchedCertificates.get(username);
    }

    public boolean disconnectClient(String username) {
        usernames.remove(username);
        boolean disconnect = usernames.remove(username);
        if (disconnect) {
            certificates.remove(username);
            dispatchedCertificates.remove(username);
        }
        return disconnect;
    }
}
