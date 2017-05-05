/*
 * Bao Lab 2016
 */

package wormguides.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;

import wormguides.view.popups.SulstonTreePane;

import static java.lang.Integer.toHexString;
import static java.lang.Math.round;
import static java.lang.System.arraycopy;

import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.GOLD;
import static javafx.scene.paint.Color.WHITE;
import static javafx.scene.paint.Color.web;

/**
 * ColorHash is a number of combinations of Colors mapped to a {@link Material}. {@link
 * wormguides.controllers.Window3DController} and {@link SulstonTreePane} query this class to find the appropriate
 * color striping to apply to a cell/its lineage. This class also contains a map of the material to the opacity
 * (0.0->1.0) of the least opaque color in a Material. This is used so that the "most opaque" materials can be
 * rendered first, followed by sheerer ones.
 */

public class ColorHash {

    private final Color othersColor = WHITE;

    private final Map<Set<Color>, Material> materialHash;
    private final Map<Material, Double> opacityHash;
    private final Material highlightMaterial;
    private final Material translucentMaterial;
    private final Material noteMaterial;

    // Used for 'others' opacity
    private final HashMap<Double, Material> opacityMaterialHash;

    public ColorHash() {
        materialHash = new HashMap<>();
        opacityHash = new HashMap<>();

        opacityMaterialHash = new HashMap<>();
        makeOthersMaterial(1.0);

        highlightMaterial = makeMaterial(GOLD);
        translucentMaterial = makeMaterial(web("#555555", 0.40));
        makeMaterial(WHITE);
        noteMaterial = makeMaterial(web("#749bc9"));
    }

    public Material getNoteSceneElementMaterial() {
        return noteMaterial;
    }

    public Material getOthersMaterial(double opacity) {
        if (opacityMaterialHash.get(opacity) == null) {
            final Material material = makeOthersMaterial(opacity);
            opacityMaterialHash.put(opacity, material);
            opacityHash.put(material, opacity);
        }

        return opacityMaterialHash.get(opacity);
    }

    /**
     * Creates a material for the 'other' cells
     *
     * @param opacity
     *         opacity between 0 and 1
     *
     * @return the material created
     */
    private Material makeOthersMaterial(double opacity) {
        String colorString = "#";
        final StringBuilder builder = new StringBuilder();
        builder.append(toHexString((int) (round(opacity * 255))));
        if (builder.length() < 2) {
            builder.insert(0, "0");
        }
        for (int i = 0; i < 3; i++) {
            colorString += builder.toString();
        }
        return new PhongMaterial(web(colorString, opacity));
    }

    private Material makeMaterial(final Color color) {
        final Set<Color> colors = new HashSet<>();
        colors.add(color);
        return makeMaterial(colors);
    }

    private Material makeMaterial(final Set<Color> colors) {
        final WritableImage wImage = new WritableImage(90, 90);
        final PixelWriter writer = wImage.getPixelWriter();
        final Color[] temp = colors.toArray(new Color[colors.size()]);
        double opacity = 1.0;

        final Color[] copy;
        if (colors.isEmpty()) {
            copy = new Color[1];
            copy[0] = WHITE;
        } else if (colors.size() == 1) {
            copy = temp;
        } else {
            // we want first and last color to be the same because of JavaFX
            // material wrapping bug
            copy = new Color[colors.size() + 1];
            arraycopy(temp, 0, copy, 0, colors.size());
            copy[colors.size()] = temp[0];
        }

        // Set opacity to alpha value of least opaque color
        for (Color color : copy) {
            if (color.getOpacity() < opacity) {
                opacity = color.getOpacity();
            }
        }

        // for more than two colors, we want segments
        int segmentLength = (int) (wImage.getHeight() / copy.length);
        Color color = BLACK;
        for (int i = 0; i < copy.length; i++) {
            color = copy[i];
            for (int j = i * segmentLength; j < (i + 1) * segmentLength; j++) {
                for (int k = 0; k < wImage.getWidth(); k++) {
                    writer.setColor(k, j, color);
                }
            }
        }

        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(wImage);
        opacityHash.put(material, opacity);
        return material;
    }

    /**
     * @param material
     *         the material to check
     *
     * @return opacity of the least opaque color in the material
     */
    public double getMaterialOpacity(final Material material) {
        if (material != null) {
        	Double opac = opacityHash.get(material);
        	if (opac != null) {
        		return opacityHash.get(material);
        	}
        }
        return 0.0;
    }

    public Material getHighlightMaterial() {
        return highlightMaterial;
    }

    public Material getTranslucentMaterial() {
        return translucentMaterial;
    }

    public Material getMaterial(final List<Color> colors) {
        final Set<Color> colorSet = new HashSet<>();
        if (colors != null) {
            colorSet.addAll(colors);
        }
        if (!materialHash.containsKey(colorSet)) {
            materialHash.put(colorSet, makeMaterial(colorSet));
        }
        return materialHash.get(colorSet);
    }

}
