import java.io.Serializable;

public class Message implements Serializable {

    private String base64Image;
    private String caption;

    public Message (String base64Image, String caption) {
        this.base64Image = base64Image;
        this.caption = caption;
    }

    public String getBase64Image() {
        return base64Image;
    }

    public String getCaption() {
        return caption;
    }

    public String toString() { return caption + base64Image; }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
