import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Session {

    private final AtomicInteger authenticatedClients;
    private final Set<String> usernames = new HashSet<>();
    private final Map<String, Object> certificates = new HashMap<>();
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

    public AtomicInteger getAuthenticatedClients() {
        return authenticatedClients;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
        log.put(username, false);
    }

    public Map<String, Boolean> getLog() {
        return log;
    }

    public void log(String username) {
        log.replace(username, true);
    }

    public boolean isLogged(String username) {
        return log.get(username);
    }

    public void resetLog() {
        for (String k : log.keySet()) {
            log.replace(k, false);
        }
    }

    public boolean disconnectClient(String username) {
        boolean disconnect = usernames.remove(username);
        if (disconnect) {
            certificates.remove(username);
            log.remove(username);
            resetLog();
        }
        return disconnect;
    }
}
