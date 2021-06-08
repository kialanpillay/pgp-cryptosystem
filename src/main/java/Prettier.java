import java.text.SimpleDateFormat;
import java.util.Date;

public class Prettier {

    private SimpleDateFormat sdf;

    public Prettier(){
        this.sdf = new SimpleDateFormat("HH:mm:ss");
    }

    public Prettier(String pattern){
        this.sdf = new SimpleDateFormat(pattern);
    }

    public void print(String entity, String message) {
        StringBuilder stringBuilder = new StringBuilder();
        System.out.println(stringBuilder
                .append(sdf.format(new Date()))
                .append(" ")
                .append("[")
                .append(entity.toUpperCase())
                .append("]")
                .append(" - ")
                .append(message));
    }

    public String toString(String entity, String message) {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder
                .append(sdf.format(new Date()))
                .append(" ")
                .append("[")
                .append(entity.toUpperCase())
                .append("]")
                .append(" - ")
                .append(message).toString();
    }
}
