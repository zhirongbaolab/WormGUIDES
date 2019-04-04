/*
 * Bao Lab 2016
 */

package application_src.application_model.annotation.color;

import java.util.Comparator;

import javafx.scene.paint.Color;

public class ColorComparator implements Comparator<Color> {
	@Override
	public int compare(Color c1, Color c2) {
		// start at the 5th character so that only the RGB components are used in the sort
		// format is 0xAARRGGBB
		System.out.println(c1.toString());
		return c1.toString().substring(0, 8).compareTo(c2.toString().substring(0, 8));
	}
}