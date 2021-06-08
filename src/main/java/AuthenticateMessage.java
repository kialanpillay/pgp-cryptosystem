public class AuthenticateMessage implements CommandMessage{

    private String message;

    public AuthenticateMessage(String message){
        this.message = message;
    }
}
