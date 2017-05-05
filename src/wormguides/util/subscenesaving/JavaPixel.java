/*
 * Bao Lab 2016
 */

package wormguides.util.subscenesaving;

/**
 * <dl>
 * <dt> class JavaPixel
 * <dd> a class representing a pixel in a picture
 * </dl>
 * @author Mark Guzdial
 * @version 2
 */
public class JavaPixel {
	public int x;
	public int y;
	JavaPicture my_pic;
	int value;
	private int red;
	private int green;
	private int blue;
	private int alpha;

        /** 
         * 
         * @param pic the JavaPicture object for each the JavaPixel is a pixel for
         * @param px the x coordinate of the pixel in pic
         * @param py the y coordinate of the pixel in pic
         */
	public JavaPixel ( JavaPicture pic, int px, int py )
	{
	   my_pic = pic;
	   x = px;
	   y = py;

	   value = my_pic.getBasicPixel(x,y);
	   red = (value >> 16) & 0xff;
	   green = (value >>  8) & 0xff;
	   blue = (value      ) & 0xff;
	   alpha = (value >> 24) & 0xff;
	 }

	 // these are integers [0,255]
         /**
          * Returns the red value of the pixel.
          *
          * @return the red value in an integer
          */
	 public int getRed()
	 {
	 	return red;
	 }
         
         /**
          * Sets the red value of the pixel.
          *
          * @param nuRed the red value as an integer
          */
	 public void setRed(int nuRed)
	 {
	 	red = nuRed & 0xff;
	 	this.setPixel();
	 }

         /** Returns the alpha value of the pixel.
          *
          * @return the alpha value in an integer
          */
	 public int getAlpha()
	 {
	 	return alpha;
	 }

         /**
          * Returns the green value of the pixel.
          *
          * @return the green value in an integer
          */
	 public int getGreen()
	 {
		return green;
	 }

	 // The set versions

         /**
          * Sets the green value of the pixel.
          *
          * @param nuGreen the green value as an integer
          */
	 public void setGreen(int nuGreen)
	 {
	 	green = nuGreen & 0xff;
	 	this.setPixel();
	 }

         /**
          * Returns the blue value of the pixel.
          *
          * @return the blue value in an integer
          */
	 public int getBlue()
	 {
		return blue;
	 }
         
         /**
          * Sets the blue value of the pixel.
          *
          * @param nuBlue the blue value as an integer
          */
	 public void setBlue(int nuBlue)
	 {
	 	blue = nuBlue & 0xff;
	 	this.setPixel();
	 }
         
         /**
          * actually sets the integer (or basic) value of the pixel once the separate
          * red, green, blue and alpha values are set
          *
          */
	 public void setPixel()
	 {
	 	value = (alpha << 24) + (red << 16) + (green << 8) + blue;
	 	my_pic.setBasicPixel(x,y,value);
	 }
}
