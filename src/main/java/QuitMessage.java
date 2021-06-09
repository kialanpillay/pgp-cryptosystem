public class QuitMessage implements CommandMessage{

    private final String message;

    public QuitMessage(String message){
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
