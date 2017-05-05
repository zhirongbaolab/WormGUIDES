/*
 * Bao Lab 2017
 */

package wormguides.stories;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import wormguides.models.subscenegeometry.SceneElement;
import wormguides.models.subscenegeometry.SceneElementsList;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.Integer.parseInt;
import static java.lang.String.join;
import static java.util.Objects.requireNonNull;

import static partslist.PartsList.getLineageNamesByFunctionalName;
import static partslist.PartsList.isFunctionalName;
import static wormguides.stories.Note.Attachment.BLANK;
import static wormguides.stories.Note.Attachment.CELL;
import static wormguides.stories.Note.Attachment.LOCATION;
import static wormguides.stories.Note.Attachment.STRUCTURE;
import static wormguides.stories.Note.Display.BILLBOARD;
import static wormguides.stories.Note.Display.BILLBOARD_FRONT;
import static wormguides.stories.Note.Display.CALLOUT_LOWER_LEFT;
import static wormguides.stories.Note.Display.CALLOUT_LOWER_RIGHT;
import static wormguides.stories.Note.Display.CALLOUT_UPPER_LEFT;
import static wormguides.stories.Note.Display.CALLOUT_UPPER_RIGHT;
import static wormguides.stories.Note.Display.OVERLAY;
import static wormguides.stories.Note.Display.SPRITE;

/**
 * This class represents a note that belongs to a story (its parent). A note contains a tag name, tag contents, an
 * attachment type, and a tag display. It may contain a location to which it belongs in the subscene, a cell to which
 * it is attached to, a marker name, an imaging attached to, a start/end time, and comments.
 * <p>
 * Notes can appears as sprites, 3D billboards, front-facing 3D billboards, or as text in the info pane. This is
 * dictated by {@link Note.Display}. If blank, the note is without scope and does not appear in the subscene.
 * <p>
 * Notes can be attached to entities such as cells, multicellular structures, or a location in the 3D subscene. This
 * is dictated by {@link Attachment}. If a note is attached to a cell, structure, or location, but the cell,
 * structure, or location is not specified, the note is without scope and does not appear in the subscene.
 */
public class Note {

    private static final String OBJ_EXT = ".obj";
    private static final String PNG_EXT = ".png";
    private static final String JPG_EXT = ".jpg";

    /** The parent story */
    private final Story parentStory;

    /** True when any field value changes, false otherwise */
    private final BooleanProperty changedProperty;

    /** True when the note is visible in the subscene, false otherwise */
    private final BooleanProperty visibleProperty;

    /** True when graphic in the stories list view is expanded, false otherwise */
    private final BooleanProperty listExpandedProperty;

    /** True when graphic in 3d subscene is expanded, false otherwise */
    private final BooleanProperty sceneExpandedProperty;

    /** True when graphical representation is selected, false otherwise */
    private final BooleanProperty activeProperty;

    /**
     * List of scene elements rendered with the note. It is possible for a note to have multiple scene elements just by
     * setting its resource location.
     */
    private List<SceneElement> elements;

    private String tagName;
    private String tagContents;
    private Attachment attachmentType;
    private Display tagDisplay;
    private int x, y, z;
    private String cellName;
    private String marker;
    private String imagingSource;
    private String resourceLocation;
    private int startTime, endTime;
    private String comments;
    private String colorUrl;

    /**
     * Horizontal offset from left/right side of the entity that the note is attached to (only applicable to notes
     * that are callouts). Upper/lower left callouts get offset more to the left of the entity by this while
     * upper/lower right callouts get offset more to the right.
     */
    private int calloutHorizontalOffset;

    /**
     * Vertical offset from the top/bottom of the entity that the note is attached to (only applicable to notes
     * that are callouts). Upper left/right callouts get offset higher above the entity by this while lower
     * left/right callouts get offset lower below.
     */
    private int calloutVerticalOffset;

    public Note(final Story parentStory) {
        this.parentStory = requireNonNull(parentStory);
        elements = null;
        tagName = "";
        tagContents = "";
        x = y = z = MIN_VALUE;
        cellName = "";
        marker = "";
        imagingSource = "";
        resourceLocation = "";
        startTime = endTime = MIN_VALUE;
        comments = "";
        changedProperty = new SimpleBooleanProperty(false);
        changedProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setChanged(false);
            }
        });
        listExpandedProperty = new SimpleBooleanProperty(false);
        sceneExpandedProperty = new SimpleBooleanProperty(false);
        visibleProperty = new SimpleBooleanProperty(true);
        activeProperty = new SimpleBooleanProperty(false);

        setDisplay(OVERLAY);
        setAttachmentType(BLANK);

        calloutHorizontalOffset = 0;
        calloutVerticalOffset = 0;
    }

    public Note(
            final Story parentStory,
            final String tagName,
            final String tagContents) {
        this(parentStory);
        if (tagName != null) {
            setTagName(tagName);
        }
        if (tagContents != null) {
            setTagContents(tagContents);
        }
    }

    public String getColorUrl() {
        return colorUrl;
    }

    public boolean hasColorScheme() {
        return colorUrl != null && !colorUrl.isEmpty();
    }

    public void setColorUrl(final String colorUrl) {
        this.colorUrl = colorUrl;
    }

    public String getLocationString() {
        if (x == MIN_VALUE || y == MIN_VALUE || z == MIN_VALUE) {
            return "";
        }
        return x + " " + y + " " + z;
    }

    public BooleanProperty getActiveProperty() {
        return activeProperty;
    }

    public boolean isActive() {
        return activeProperty.get();
    }

    public void setActive(final boolean active) {
        activeProperty.set(active);
    }

    public BooleanProperty getVisibleProperty() {
        return visibleProperty;
    }

    public boolean isVisible() {
        return visibleProperty.get();
    }

    public void setVisible(final boolean visible) {
        visibleProperty.set(visible);
    }

    public BooleanProperty getSceneExpandedProperty() {
        return sceneExpandedProperty;
    }

    public boolean isExpandedInScene() {
        return sceneExpandedProperty.get();
    }

    public void setExpandedInScene(final boolean expanded) {
        sceneExpandedProperty.set(expanded);
    }

    public BooleanProperty getListExpandedProperty() {
        return listExpandedProperty;
    }

    public boolean isListExpanded() {
        return listExpandedProperty.get();
    }

    public void setListExpanded(final boolean expanded) {
        listExpandedProperty.set(expanded);
    }

    public BooleanProperty getChangedProperty() {
        return changedProperty;
    }

    public void setChanged(final boolean changed) {
        changedProperty.set(changed);
    }

    public boolean changed() {
        return changedProperty.get();
    }

    public Story getParentStory() {
        return parentStory;
    }

    public void setTagDisplay(String display) throws TagDisplayEnumException {
        if (display != null) {
            display = display.trim();
            for (Display d : Display.values()) {
                if (d.equals(display)) {
                    setDisplay(d);
                    return;
                }
            }
            throw new TagDisplayEnumException();
        }
    }

    public void setAttachmentType(String type) throws AttachmentTypeEnumException {
        if (type != null) {
            type = type.trim();
            for (Attachment t : Attachment.values()) {
                if (t.equals(type)) {
                    setAttachmentType(t);
                    return;
                }
            }
            throw new AttachmentTypeEnumException();
        }
    }

    public void setLocation(final String location) throws LocationStringFormatException {
        if (location != null && !location.isEmpty()) {
            final String[] coords = location.trim().split(" ");
            if (coords.length != 3) {
                throw new LocationStringFormatException();
            }
            try {
                setLocation(
                        parseInt(coords[0]),
                        parseInt(coords[1]),
                        parseInt(coords[2]));
            } catch (NumberFormatException e) {
                throw new LocationStringFormatException();
            }
        }
    }

    public void setLocation(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;

        if (elements != null) {
            for (SceneElement se : elements) {
                se.setLocation(x, y, z);
            }
        }
    }

    public void setImagingSource(final String source) {
        if (source != null
                && !source.isEmpty()
                && source.trim().toLowerCase().endsWith(OBJ_EXT)) {

            imagingSource = source.trim();

            if (elements != null) {
                for (SceneElement se : elements) {
                    se.setImagingSource(imagingSource);
                }
            }
        }
    }

    public boolean hasSceneElements() {
        return elements != null && !elements.isEmpty();
    }

    public void setStartAndEndTimes(final int start, final int end) {
        startTime = start;
        endTime = end;
        if (elements != null) {
            for (SceneElement se : elements) {
                se.setStartTime(startTime);
                se.setEndTime(endTime);
            }
        }
    }

    public void addSceneElementsToList(final SceneElementsList list) {
        if (list != null && elements != null) {
            elements.forEach(list::addSceneElement);
        }
    }

    public List<SceneElement> getSceneElements() {
        return elements;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(final String tagName) {
        if (tagName != null) {
            this.tagName = tagName;
        }
        if (elements != null) {
            for (SceneElement se : elements) {
                se.setSceneName(tagName);
            }
        }
    }

    public String getTagContents() {
        return tagContents;
    }

    public void setTagContents(final String tagContents) {
        if (tagContents != null) {
            this.tagContents = tagContents;
        }
    }

    public Display getTagDisplay() {
        return tagDisplay;
    }

    public void setDisplay(final Display display) {
        if (display != null) {
            tagDisplay = display;
        }
    }

    public Attachment getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(final Attachment attachment) {
        if (attachment != null) {
            attachmentType = attachment;
        }
    }

    public int getCalloutHorizontalOffset() {
        return calloutHorizontalOffset;
    }

    public void setCalloutHorizontalOffset(final int horizontalOffset) {
        calloutHorizontalOffset = horizontalOffset;
    }

    public int getCalloutVerticalOffset() {
        return calloutVerticalOffset;
    }

    public void setCalloutVerticalOffset(final int verticalOffset) {
        calloutVerticalOffset = verticalOffset;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getCellName() {
        return cellName;
    }

    public void setCellName(final String name) {
        if (name != null) {
            cellName = name.trim();
            if (isFunctionalName(cellName)) {
                cellName = getLineageNamesByFunctionalName(cellName).get(0);
            }
            if (elements != null) {
                for (SceneElement se : elements) {
                    se.addCellName(cellName);
                }
            }
        }
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(final String marker) {
        if (marker != null) {
            this.marker = marker.trim();

            if (elements != null) {
                for (SceneElement se : elements) {
                    se.setMarker(this.marker);
                }
            }
        }
    }

    public String getImgSource() {
        return imagingSource;
    }

    public String getResourceLocation() {
        return resourceLocation;
    }

    /**
     * Sets the resource location for this note
     * Update 02/2017: Check to see if an image file name is in the resource location field. If so, we'll enable the
     * flag indicating that this note should not display its text but rather render the image contained in this path
     * at the location of attachment
     *
     * @param location
     *         the resource location
     */
    public void setResourceLocation(final String location) {
        if (elements == null) {
            elements = new ArrayList<>();
        }
        if (location != null && !location.isEmpty()) {
            // first check if this is an obj file
            // then check if it is an image file
            // then go into the failure mode
            resourceLocation = location.trim();
            String sceneName = resourceLocation;
            if (resourceLocation.lastIndexOf("/") != -1) {
                sceneName = resourceLocation.substring(resourceLocation.lastIndexOf("/") + 1);
            }
            final SceneElement se = new SceneElement(
                    sceneName,
                    cellName,
                    marker,
                    imagingSource,
                    resourceLocation,
                    startTime,
                    endTime + 1,
                    comments);
            se.setLocation(x, y, z);
            elements.add(se);
        }
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int time) {
        if (time >= 1) {
            startTime = time;

            if (elements != null) {
                for (SceneElement se : elements) {
                    se.setStartTime(startTime);
                }
            }
        }
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int time) {
        if (time >= 1) {
            endTime = time;

            if (elements != null) {
                for (SceneElement se : elements) {
                    se.setEndTime(endTime);
                }
            }
        }
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        if (comments != null) {
            this.comments = comments.trim();

            if (elements != null) {
                for (SceneElement se : elements) {
                    se.setComments(this.comments);
                }
            }
        }
    }

    public boolean isTagDisplayEnum(String display) {
        for (Display d : Display.values()) {
            if (d.equals(display)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells whether a note is without scope. Such a note is not displayed. Not having scope is defined by any of the
     * following combinations:
     * 1. time is not specified for the CELLTIME or TIME attachment types
     * 2. cell name is not specified for the CELLTIME or CELL attachment types
     * 3. no tag display methods is specified
     *
     * @return true if the note is without scope, false otherwise
     */
    public boolean isWithoutScope() {
        return tagDisplay.equals(Display.BLANK)
                || !tagDisplay.equals(OVERLAY)
                && (attachmentType.equals(CELL) && !isEntitySpecified());

    }

    public boolean hasLocationError() {
        return attachmentType.equals(LOCATION) && !isLoctionSpecified();

    }

    public boolean hasEntityNameError() {
        return !tagDisplay.equals(OVERLAY)
                && (attachedToCell() || attachedToStructure())
                && cellName.isEmpty();

    }

    public boolean attachedToStructure() {
        return attachmentType.equals(STRUCTURE);
    }

    public boolean attachedToCell() {
        return attachmentType.equals(CELL);
    }

    public boolean attachedToLocation() {
        return attachmentType.equals(LOCATION);
    }

    public boolean attachedToGlobalEvent() {
        return attachmentType.equals(BLANK);
    }

    public boolean isEntitySpecified() {
        return !cellName.isEmpty();
    }

    public boolean isTimeSpecified() {
        return startTime >= 0 && endTime >= 0;
    }

    /**
     * @param time
     *         time to check
     *
     * @return true if note is visible at input time, or in sprite cell/celltime mode, false otherwise
     */
    public boolean mayExistAtTime(int time) {
        if (!isWithoutScope()) {
            // If start and end times are not set
            // then note exists at all times
            if (!isTimeSpecified()) {
                return true;
            }
            if (startTime <= time && time <= endTime) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return true if a location is specified for this note, false otherwise
     */
    public boolean isLoctionSpecified() {
        return (x != MIN_VALUE
                && y != MIN_VALUE
                && z != MIN_VALUE);
    }

    public boolean isOverlay() {
        return tagDisplay == OVERLAY;
    }

    public boolean isSprite() {
        return tagDisplay == SPRITE;
    }

    public boolean isCallout() {
        return tagDisplay == CALLOUT_LOWER_LEFT
                || tagDisplay == CALLOUT_LOWER_RIGHT
                || tagDisplay == CALLOUT_UPPER_LEFT
                || tagDisplay == CALLOUT_UPPER_RIGHT;
    }

    public boolean isBillboard() {
        return tagDisplay == BILLBOARD;
    }

    public boolean isBillboardFront() {
        return tagDisplay == BILLBOARD_FRONT;
    }

    public String toString() {
        return "Note["
                + "@Name='" + tagName + "' "
                + "@Attachment=" + attachmentType + " "
                + "@Display=" + tagDisplay + " "
                + "@Time=" + startTime + ", " + endTime + " "
                + "@Location=" + x + ", " + y + ", " + z + " "
                + "@Cell='" + cellName + "' "
                + "@Resource='" + resourceLocation + "']";
    }

    /**
     * Attachment type for a {@link Note}. This defines that a note is attached to.
     */
    public enum Attachment {

        /** Attachment to location defined by three coordinate **/
        LOCATION("location"),

        /** Attachment to a cell */
        CELL("cell"),

        /** Attachment to a structure */
        STRUCTURE("structure"),

        /** No attachment - note becomes an overlay */
        BLANK("");

        private final String type;

        Attachment(final String type) {
            this.type = type.trim();
        }

        public static String valuesToString() {
            final List<String> values = new ArrayList<>();
            for (Attachment attachment : values()) {
                values.add(attachment.toString());
            }
            return join(",", values);
        }

        @Override
        public String toString() {
            return type;
        }

        public boolean equals(final String otherType) {
            return type.equalsIgnoreCase(otherType.trim());
        }

        public boolean equals(final Attachment attachment) {
            return this == attachment;
        }
    }

    /**
     * Display mode for a {@link Note}
     */
    public enum Display {

        /** Display as an overlay in the upper-right-hand corner of the 3D subscene **/
        OVERLAY("overlay"),

        /**
         * Display as a 3D billboard that rotates and translates with the entity that the note is attached to
         * (defined by
         * {@link Attachment})
         */
        BILLBOARD("billboard"),

        /**
         * Display as a front-facing billboard that translates with the entity that the note is attached to (defined by
         * {@link Attachment})
         */
        BILLBOARD_FRONT("billboard front"),

        /**
         * Display as a sprite that moves with the entity that the note is attached to (defined by
         * {@link Attachment})
         */
        SPRITE("sprite"),

        /**
         * Display as a sprite offset to the upper left of the entity that the note is attached to. A line is drawn from
         * the note to the left side of the entity.
         */
        CALLOUT_UPPER_LEFT("callout UL"),

        /**
         * Display as a sprite offset to the lower left of the entity that the note is attached to. A line is drawn from
         * the note to the left side of the entity.
         */
        CALLOUT_LOWER_LEFT("callout LL"),

        /**
         * Display as a sprite to offset the upper right of the entity that the note is attached to. A line is drawn
         * from the note to the right side of the entity.
         */
        CALLOUT_UPPER_RIGHT("callout UR"),

        /**
         * Display as a sprite to the offset lower right of the entity that the note is attached to. A line is drawn
         * from the note to the right side of the entity.
         */
        CALLOUT_LOWER_RIGHT("callout LR"),

        /** No display, defaults to overlay in the upper-right-hand corner of the 3D subscene */
        BLANK("");

        private final String display;

        Display(final String display) {
            this.display = display.trim();
        }

        public static String valuesToString() {
            final List<String> values = new ArrayList<>();
            for (Display display : values()) {
                values.add(display.toString());
            }
            return join(",", values);
        }

        @Override
        public String toString() {
            return display;
        }

        public boolean equals(final String otherDisplay) {
            return display.equalsIgnoreCase(otherDisplay.trim());
        }

        public boolean equals(final Display display) {
            return this == display;
        }
    }

    public class TagDisplayEnumException extends Exception {
        // default variable needed for some reason
        private static final long serialVersionUID = 1L;

        public TagDisplayEnumException() {
            super("Invalid note tag display enum, must be one of the following:\n" + Display.valuesToString());
        }
    }

    public class AttachmentTypeEnumException extends Exception {
        // default variable needed for some reason
        private static final long serialVersionUID = 1L;

        public AttachmentTypeEnumException() {
            super("Invalid note attachment type enum, must be one of the following:\n" + Attachment.valuesToString());
        }
    }

    public class LocationStringFormatException extends Exception {
        // default variable needed for some reason
        private static final long serialVersionUID = 1L;

        public LocationStringFormatException() {
            super("Invalid note location string format, must be 3 integers separated by spaces.");
        }
    }
}
