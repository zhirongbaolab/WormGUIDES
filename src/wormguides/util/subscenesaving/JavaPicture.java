/*
 * Bao Lab 2016
 */

package wormguides.util.subscenesaving;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class JavaPicture {

    public BufferedImage bimg;
    public JFrame shower = new JFrame();
    public ImageIcon imgIcon;
    public Canvas canvas;
    public Image image;

    public int getWidth() {
        return bimg.getWidth();
    }

    public int getHeight() {
        return bimg.getHeight();
    }

    /**
     * Loads am image from a file
     *
     * @param file
     *         the file to read
     */
    public boolean loadImage(final File file) {
        try {
            image = ImageIO.read(file);
            final MediaTracker mediaTracker = new MediaTracker(shower);
            mediaTracker.addImage(image, 0);
            try {
                mediaTracker.waitForID(0);
            } catch (InterruptedException ie) {
                //The file did not load
                ie.printStackTrace();
            }

            bimg = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
            final Graphics g = bimg.getGraphics();
            g.drawImage(image, 0, 0, null);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public void createNewImage(int width, int height) {
        bimg = null;
        bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public void repaintImage() {
        if (shower.isVisible()) {
            imgIcon.setImage(bimg.getScaledInstance(bimg.getWidth(), bimg.getHeight(), Image.SCALE_FAST));
            shower.repaint();
        }
    }

    public void showPictureWithTitle(String s) {
        if (shower.isVisible()) {
            imgIcon.setImage(bimg.getScaledInstance(bimg.getWidth(), bimg.getHeight(), Image.SCALE_FAST));
            shower.setTitle(s);
            shower.repaint();
        } else {
            shower = new JFrame(s);
            imgIcon = new ImageIcon(bimg.getScaledInstance(bimg.getWidth(), bimg.getHeight(), Image.SCALE_FAST));
            shower.getContentPane().add(new JLabel(imgIcon));
            shower.setResizable(false);
            shower.pack();
            shower.setVisible(true);
        }
    }

    /**
     * Saves the image represented by the JavaPicture object onto disk.
     *
     * @param newfilename
     *         the file name to save to
     *
     * @throws IOException
     *         when the save fails
     */
    public boolean saveImage(String newfilename) throws IOException {

        FileOutputStream out;
        JPEGImageEncoder jpeg;
        File filen;
        try {
            filen = new File(newfilename);
            //if (filen.canWrite()){
            //return false;}
            out = new FileOutputStream(filen);
        } catch (Exception e) {
            System.out.println("Sorry -- that filename (" + newfilename + ") isn't working");
            return false;
        }

        try {
            jpeg = JPEGCodec.createJPEGEncoder(out);
        } catch (Exception e) {
            System.out.println("Unable to create a JPEG encoder");
            return false;
        }

        JPEGEncodeParam param = jpeg.getDefaultJPEGEncodeParam(bimg);
        param.setQuality(1.0f, true);
        jpeg.encode(bimg, param);
        out.close();
        return true;
    }

    /**
     * Returns the pixel value of a pixel in the picture, given its coordinates.
     *
     * @param x
     *         the x coordinate of the pixel
     * @param y
     *         the y coordinate of the pixel
     *
     * @return the pixel value as an integer
     */
    public int getBasicPixel(int x, int y) {
        // to access pixel at row 'j' and column 'i' from the upper-left corner of
        // image.
        return bimg.getRGB(x, y);
    }

    /**
     * Sets the value of a pixel in the picture.
     *
     * @param x
     *         the x coordinate of the pixel
     * @param y
     *         the y coordinate of the pixel
     * @param rgb
     *         the new rgb value of the pixel
     */
    public void setBasicPixel(int x, int y, int rgb) {
        bimg.setRGB(x, y, rgb);
    }

    /**
     * Returns a JavaPixel object representing a pixel in the picture given its coordinates
     *
     * @param x
     *         the x coordinates of the pixel
     * @param y
     *         the y coordinates of the pixel
     *
     * @return a JavaPixel object representing the requested pixel
     */
    public JavaPixel getPixel(int x, int y) {
        return new JavaPixel(this, x, y);
    }
}
        
        
        

