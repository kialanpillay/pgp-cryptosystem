import java.io.Serializable;

/**
 * <code>Message</code> is an concrete class that represents an unencrypted message
 * transmitted between two clients over a network.
 * A <code>Message</code> consists of the <code>Base64</code> encoding of an image and associated
 * caption.
 *
 * @author Kialan Pillay
 * @version %I%, %G%
 */
public class Message implements Serializable {

    private String base64Image;
    private String caption;

    /**
     * Class constructor specifying image and caption
     *
     * @param base64Image <code>Base64</code> encoded image
     * @param caption     image caption
     */
    public Message(String base64Image, String caption) {
        this.base64Image = base64Image;
        this.caption = caption;
    }

    /**
     * Returns <code>Base64</code> encoding of an image
     *
     * @return <code>String</code>
     */
    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }

    /**
     * Returns the caption associated with an image
     *
     * @return <code>String</code>
     */
    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String toString() { return caption + base64Image; }
}
