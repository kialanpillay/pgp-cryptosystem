public class AuthenticateMessage implements CommandMessage{

    private final String message;

    public AuthenticateMessage(String message){
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
