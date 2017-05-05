package wormguides.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageLoader {

    private static final String ENTRY_PREFIX = "/wormguides/view/icons/", PATH_FROM_ROOT = "wormguides/view/icons/",
            BACKWARD_PNG = "backward.png", FORWARD_PNG = "forward.png", PAUSE_PNG = "pause.png", PLAY_PNG = "play.png",
            EDIT_PNG = "edit.png", EYE_PNG = "eye.png", EYE_INV_PNG = "eye-invert.png", CLOSE_PNG = "close.png",
            PLUS_PNG = "plus.png", MINUS_PNG = "minus.png", COPY_PNG = "copy.png", PASTE_PNG = "paste.png";
    private static ImageView forward, backward, play, pause;
    private static Image plus, minus;
    private static Image edit, eye, eyeInvert, close;
    private static Image copy;
    private static ImageView paste;

    public static void loadImages() {

        try {
            URL urlBack = ImageLoader.class.getResource(ENTRY_PREFIX + BACKWARD_PNG);
            processImage(urlBack);

            URL urlFor = ImageLoader.class.getResource(ENTRY_PREFIX + FORWARD_PNG);
            processImage(urlFor);

            URL urlClose = ImageLoader.class.getResource(ENTRY_PREFIX + CLOSE_PNG);
            processImage(urlClose);

            URL urlCopy = ImageLoader.class.getResource(ENTRY_PREFIX + COPY_PNG);
            processImage(urlCopy);

            URL urlEdit = ImageLoader.class.getResource(ENTRY_PREFIX + EDIT_PNG);
            processImage(urlEdit);

            URL urlEye = ImageLoader.class.getResource(ENTRY_PREFIX + EYE_PNG);
            processImage(urlEye);

            URL urlEyei = ImageLoader.class.getResource(ENTRY_PREFIX + EYE_INV_PNG);
            processImage(urlEyei);

            URL urlMinus = ImageLoader.class.getResource(ENTRY_PREFIX + MINUS_PNG);
            processImage(urlMinus);

            URL urlPaste = ImageLoader.class.getResource(ENTRY_PREFIX + PASTE_PNG);
            processImage(urlPaste);

            URL urlPause = ImageLoader.class.getResource(ENTRY_PREFIX + PAUSE_PNG);
            processImage(urlPause);

            URL urlPlay = ImageLoader.class.getResource(ENTRY_PREFIX + PLAY_PNG);
            processImage(urlPlay);

            URL urlPlus = ImageLoader.class.getResource(ENTRY_PREFIX + PLUS_PNG);
            processImage(urlPlus);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void processImage(URL url) throws IOException {
        if (url != null) {
            InputStream input = url.openStream();
            Image image = new Image(input);
            String urlStr = url.getFile();
            urlStr = urlStr.substring(urlStr.indexOf("wormguides"));
            switch (urlStr) {
                case PATH_FROM_ROOT + EDIT_PNG:
                    edit = image;
                    return;
                case PATH_FROM_ROOT + EYE_PNG:
                    eye = image;
                    return;
                case PATH_FROM_ROOT + EYE_INV_PNG:
                    eyeInvert = image;
                    return;
                case PATH_FROM_ROOT + CLOSE_PNG:
                    close = image;
                    return;
                case PATH_FROM_ROOT + COPY_PNG:
                    copy = image;
                    return;
                case PATH_FROM_ROOT + PLUS_PNG:
                    plus = image;
                    return;
                case PATH_FROM_ROOT + MINUS_PNG:
                    minus = image;
                    return;
            }
            ImageView icon = new ImageView(image);
            switch (urlStr) {
                case PATH_FROM_ROOT + BACKWARD_PNG:
                    backward = icon;
                    break;
                case PATH_FROM_ROOT + FORWARD_PNG:
                    forward = icon;
                    break;
                case PATH_FROM_ROOT + PLAY_PNG:
                    play = icon;
                    break;
                case PATH_FROM_ROOT + PAUSE_PNG:
                    pause = icon;
                    break;
                case PATH_FROM_ROOT + PASTE_PNG:
                    paste = icon;
            }
        }
    }

    public static ImageView getForwardIcon() {
        return forward;
    }

    public static ImageView getBackwardIcon() {
        return backward;
    }

    public static ImageView getPlayIcon() {
        return play;
    }

    public static ImageView getPauseIcon() {
        return pause;
    }

    public static Image getPlusIcon() {
        return plus;
    }

    public static Image getMinusIcon() {
        return minus;
    }

    public static ImageView getEditIcon() {
        return new ImageView(edit);
    }

    public static ImageView getEyeIcon() {
        return new ImageView(eye);
    }

    public static ImageView getEyeInvertIcon() {
        return new ImageView(eyeInvert);
    }

    public static ImageView getCloseIcon() {
        return new ImageView(close);
    }

    public static ImageView getCopyIcon() {
        return new ImageView(copy);
    }

    public static ImageView getPasteIcon() {
        return paste;
    }
}
