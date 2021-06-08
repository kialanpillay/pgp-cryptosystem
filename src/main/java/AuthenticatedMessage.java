public class AuthenticatedMessage implements CommandMessage{

    private String message;

    public AuthenticatedMessage(String message){
        this.message = message;
    }
}
