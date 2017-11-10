package application_src.application_model.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Loader for billboard images attached to notes
 */
public class NoteImageLoader {

    /**
     * Creates and returns the {@link ImageView} of the image specified by a url
     *
     * @param imageUrl
     *         the path to the image
     *
     * @return the image view, null if the image path is invalid
     */
    public static ImageView createImageView(final String imageUrl) {
        final URL url = NoteImageLoader.class.getResource(imageUrl);
        if (url != null) {
            try {
                final InputStream input = url.openStream();
                return new ImageView(new Image(input));
            } catch (IOException e) {
                System.out.println("Note image path " + imageUrl + " is invalid.");
                return null;
            }
        }
        return null;
    }
}
