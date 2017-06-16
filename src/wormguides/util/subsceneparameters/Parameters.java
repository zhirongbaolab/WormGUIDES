/*
 * Bao Lab 2017
 */

package wormguides.util.subsceneparameters;

import java.util.Map;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

import static wormguides.loaders.ParametersLoader.PARAMETERS_FILE_PATH;
import static wormguides.loaders.ParametersLoader.loadParameters;

/**
 * Loader and wrapper for all parameters loaded from /wormguides/util/subsceneparameters/parameters.txt
 */
public class Parameters {

    // keys
    private static final String WAIT_TIME_MILLI_KEY = "WAIT_TIME_MILLI";
    private static final String INITIAL_ZOOM_KEY = "INITIAL_ZOOM";
    private static final String INITIAL_TRANSLATE_X_KEY = "INITIAL_TRANSLATE_X";
    private static final String INITIAL_TRANSLATE_Y_KEY = "INITIAL_TRANSLATE_Y";
    private static final String CAMERA_INITIAL_DISTANCE_KEY = "CAMERA_INITIAL_DISTANCE";
    private static final String CAMERA_NEAR_CLIP_KEY = "CAMERA_NEAR_CLIP";
    private static final String CAMERA_FAR_CLIP_KEY = "CAMERA_FAR_CLIP";
    private static final String BILLBOARD_SCALE_KEY = "BILLBOARD_SCALE";
    private static final String SIZE_SCALE_KEY = "SIZE_SCALE";
    private static final String UNIFORM_RADIUS_KEY = "UNIFORM_RADIUS";
    private static final String LABEL_SPRITE_Y_OFFSET_KEY = "LABEL_SPRITE_Y_OFFSET";
    private static final String DEFAULT_OTHERS_OPACITY_KEY = "DEFAULT_OTHERS_OPACITY";
    private static final String VISIBILITY_CUTOFF_KEY = "VISIBILITY_CUTOFF";
    private static final String SELECTABILITY_VISIBILITY_CUTOFF_KEY = "SELECTABILITY_VISIBILITY_CUTOFF";
    private static final String STORY_OVERLAY_PANE_WIDTH_KEY = "STORY_OVERLAY_PANE_WIDTH";
    private static final String NOTE_SPRITE_TEXT_WIDTH_KEY = "NOTE_SPRITE_TEXT_WIDTH";
    private static final String NOTE_BILLBOARD_TEXT_WIDTH_KEY = "NOTE_BILLBOARD_TEXT_WIDTH";
    private static final String NOTE_IMAGE_SCALE_KEY = "NOTE_IMAGE_SCALE";
    private static final String SUBSCENE_BACKGROUND_COLOR_HEX_KEY = "SUBSCENE_BACKGROUND_COLOR_HEX";

    // values
    /** Waiting time between rendering consecutive frames while movie is playing */
    private static long waitTimeMilli;

    private static double initialZoom;

    private static double initialTranslateX;

    private static double initialTranslateY;

    private static double cameraInitialDistance;

    private static double cameraNearClip;

    private static double cameraFarClip;

    private static double billboardScale;

    /** Scale used for radii of spheres, multiplied with cell's radius from nuc */
    private static double sizeScale;

    /** Radius of all spheres in 'Uniform Size' mode*/
    private static int uniformRadius;

    /** Y-offset from sprite to label for one cell entity */
    private static int labelSpriteYOffset;

    /** Default transparency of 'other' entities on startup */
    private static double defaultOthersOpacity;

    /** Visibility in the range [0, 1] under which 'other' entities are not rendered */
    private static double visibilityCutoff;

    /** Visibility in the range [0, 1] 'other' not selectable */
    private static double selectabilityVisibilityCutoff;

    /** Width (in pixels) of the stories overlay pane */
    private static double storyOverlayPaneWidth;

    /** Wrapping width (in pixels) of the note sprites */
    private static double noteSpriteTextWidth;

    /** Wrapping width (in pixels) of the note billboards */
    private static double noteBillboardTextWidth;

    /** Scale for note billboard images */
    private static double noteBillboardImageScale;

    /** Background color (in hex) of the 3D subscene */
    private static String subsceneBackgroundColorHex;

    public static void init() {
        final Map<String, String> param_map = loadParameters();
        try {
            waitTimeMilli = parseLong(param_map.get(WAIT_TIME_MILLI_KEY));
            initialZoom = parseDouble(param_map.get(INITIAL_ZOOM_KEY));
            initialTranslateX = parseDouble(param_map.get(INITIAL_TRANSLATE_X_KEY));
            initialTranslateY = parseDouble(param_map.get(INITIAL_TRANSLATE_Y_KEY));
            cameraInitialDistance = parseDouble(param_map.get(CAMERA_INITIAL_DISTANCE_KEY));
            cameraNearClip = parseDouble(param_map.get(CAMERA_NEAR_CLIP_KEY));
            cameraFarClip = parseDouble(param_map.get(CAMERA_FAR_CLIP_KEY));
            billboardScale = parseDouble(param_map.get(BILLBOARD_SCALE_KEY));
            sizeScale = parseDouble(param_map.get(SIZE_SCALE_KEY));
            uniformRadius = parseInt(param_map.get(UNIFORM_RADIUS_KEY));
            labelSpriteYOffset = parseInt(param_map.get(LABEL_SPRITE_Y_OFFSET_KEY));
            defaultOthersOpacity = parseDouble(param_map.get(DEFAULT_OTHERS_OPACITY_KEY));
            visibilityCutoff = parseDouble(param_map.get(VISIBILITY_CUTOFF_KEY));
            selectabilityVisibilityCutoff = parseDouble(param_map.get(SELECTABILITY_VISIBILITY_CUTOFF_KEY));
            storyOverlayPaneWidth = parseDouble(param_map.get(STORY_OVERLAY_PANE_WIDTH_KEY));
            noteSpriteTextWidth = parseDouble(param_map.get(NOTE_SPRITE_TEXT_WIDTH_KEY));
            noteBillboardTextWidth = parseDouble(param_map.get(NOTE_BILLBOARD_TEXT_WIDTH_KEY));
            noteBillboardImageScale = parseDouble(param_map.get(NOTE_IMAGE_SCALE_KEY));
            subsceneBackgroundColorHex = param_map.get(SUBSCENE_BACKGROUND_COLOR_HEX_KEY);
        } catch (Exception e) {
            System.out.println("Error in parsing parameters file at " + PARAMETERS_FILE_PATH);
            e.printStackTrace();
        }
    }

    public static long getWaitTimeMilli() {
        return waitTimeMilli;
    }

    public static double getInitialZoom() {
        return initialZoom;
    }

    public static double getInitialTranslateX() {
        return initialTranslateX;
    }

    public static double getInitialTranslateY() {
        return initialTranslateY;
    }

    public static double getCameraInitialDistance() {
        return cameraInitialDistance;
    }

    public static double getCameraNearClip() {
        return cameraNearClip;
    }

    public static double getCameraFarClip() {
        return cameraFarClip;
    }

    public static double getBillboardScale() {
        return billboardScale;
    }

    public static double getSizeScale() {
        return sizeScale;
    }

    public static int getUniformRadius() {
        return uniformRadius;
    }

    public static int getLabelSpriteYOffset() {
        return labelSpriteYOffset;
    }

    public static double getDefaultOthersOpacity() {
        return defaultOthersOpacity;
    }

    public static double getVisibilityCutoff() {
        return visibilityCutoff;
    }

    public static double getSelectabilityVisibilityCutoff() {
        return selectabilityVisibilityCutoff;
    }

    public static double getStoryOverlayPaneWidth() {
        return storyOverlayPaneWidth;
    }

    public static double getNoteSpriteTextWidth() {
        return noteSpriteTextWidth;
    }

    public static double getNoteBillboardTextWidth() {
        return noteBillboardTextWidth;
    }

    public static double getNoteBillboardImageScale() {
        return  noteBillboardImageScale;
    }

    public static String getSubsceneBackgroundColorHex() {
        return subsceneBackgroundColorHex;
    }
}
