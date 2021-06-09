public class QuitMessage implements CommandMessage{

    private String message;

    public QuitMessage(String message){
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
