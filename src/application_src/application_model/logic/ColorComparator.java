/*
 * Bao Lab 2016
 */

package application_src.application_model.logic;

import java.util.Comparator;

import javafx.scene.paint.Color;

public class ColorComparator implements Comparator<Color> {
	@Override
	public int compare(Color c1, Color c2) {
		return c1.toString().compareTo(c2.toString());
	}
}