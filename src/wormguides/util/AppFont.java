/*
 * Bao Lab 2016
 */

package wormguides.util;

import javafx.scene.text.Font;

import static javafx.scene.text.Font.font;
import static javafx.scene.text.FontWeight.BOLD;
import static javafx.scene.text.FontWeight.EXTRA_BOLD;
import static javafx.scene.text.FontWeight.SEMI_BOLD;

/**
 * Different fonts used throughout the application
 */
public class AppFont {

    private static final String SYSTEM = "System";

    private static final Font FONT = font(14);

    private static final Font BOLD_FONT = font(SYSTEM, SEMI_BOLD, 14);

    private static final Font BOLDER_FONT = font(SYSTEM, EXTRA_BOLD, 14);

    private static final Font BILLBOARD_FONT = font(SYSTEM, BOLD, 10);

    private static final Font SPRITE_AND_OVERLAY_FONT = font(SYSTEM, BOLD, 16);

    public static Font getSpriteAndOverlayFont() {
        return SPRITE_AND_OVERLAY_FONT;
    }

    public static Font getBillboardFont() {
        return BILLBOARD_FONT;
    }

    public static Font getFont() {
        return FONT;
    }

    public static Font getBolderFont() {
        return BOLDER_FONT;
    }

    public static Font getBoldFont() {
        return BOLD_FONT;
    }
}
